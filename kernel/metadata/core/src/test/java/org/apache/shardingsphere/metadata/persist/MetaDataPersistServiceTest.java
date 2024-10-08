/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.metadata.persist;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.service.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({TypedSPILoader.class, DataSourcePoolPropertiesCreator.class, GenericSchemaManager.class})
class MetaDataPersistServiceTest {
    
    @Mock
    private DataSourceUnitPersistService dataSourceUnitService;
    
    @Mock
    private DatabaseMetaDataPersistService databaseMetaDataService;
    
    @Mock
    private DatabaseRulePersistService databaseRulePersistService;
    
    @Mock
    private GlobalRulePersistService globalRuleService;
    
    @Mock
    private PropertiesPersistService propsService;
    
    private MetaDataPersistService metaDataPersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        metaDataPersistService = new MetaDataPersistService(mock(PersistRepository.class));
        setField("dataSourceUnitService", dataSourceUnitService);
        setField("databaseMetaDataService", databaseMetaDataService);
        setField("databaseRulePersistService", databaseRulePersistService);
        setField("globalRuleService", globalRuleService);
        setField("propsService", propsService);
    }
    
    private void setField(final String name, final Object value) throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(MetaDataPersistService.class.getDeclaredField(name), metaDataPersistService, value);
    }
    
    @Test
    void assertPersistGlobalRuleConfiguration() {
        Collection<RuleConfiguration> globalRuleConfigs = Collections.singleton(mock(RuleConfiguration.class));
        Properties props = new Properties();
        metaDataPersistService.persistGlobalRuleConfiguration(globalRuleConfigs, props);
        verify(globalRuleService).persist(globalRuleConfigs);
        verify(propsService).persist(props);
    }
    
    @Test
    void assertPersistConfigurationsWithEmptyDatabase() {
        metaDataPersistService.persistConfigurations("foo_db", mock(DatabaseConfiguration.class), Collections.emptyMap(), Collections.emptyList());
        verify(databaseMetaDataService).addDatabase("foo_db");
    }
    
    @Test
    void assertPersistConfigurationsWithDatabaseRuleConfigurations() {
        DatabaseConfiguration databaseConfig = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        when(databaseConfig.getStorageUnits()).thenReturn(Collections.emptyMap());
        when(databaseConfig.getRuleConfigurations().isEmpty()).thenReturn(false);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass())).thenReturn(Optional.of(mock(RuleConfigurationDecorator.class)));
        metaDataPersistService.persistConfigurations("foo_db", databaseConfig, Collections.emptyMap(), Collections.singleton(rule));
        verify(dataSourceUnitService).persist("foo_db", Collections.emptyMap());
        verify(databaseRulePersistService).persist(eq("foo_db"), any());
    }
    
    @Test
    void assertPersistConfigurationsWithDataSourcePoolProperties() {
        DatabaseConfiguration databaseConfig = mock(DatabaseConfiguration.class, RETURNS_DEEP_STUBS);
        when(databaseConfig.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class, RETURNS_DEEP_STUBS)));
        metaDataPersistService.persistConfigurations("foo_db", databaseConfig, Collections.emptyMap(), Collections.emptyList());
        verify(dataSourceUnitService).persist(eq("foo_db"), any());
        verify(databaseRulePersistService).persist("foo_db", Collections.emptyList());
    }
    
    @Test
    void assertLoadDataSourceConfigurations() {
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class);
        when(dataSourceUnitService.load("foo_db")).thenReturn(Collections.singletonMap("foo_ds", dataSourcePoolProps));
        DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
        when(DataSourcePoolPropertiesCreator.createConfiguration(dataSourcePoolProps)).thenReturn(dataSourceConfig);
        Map<String, DataSourceConfiguration> actual = metaDataPersistService.loadDataSourceConfigurations("foo_db");
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_ds"), is(dataSourceConfig));
    }
    
    @Test
    void assertPersistReloadDatabaseByAlter() {
        ShardingSphereSchema toBeDeletedSchemas = mock(ShardingSphereSchema.class);
        ShardingSphereSchema toBeAddedSchemas = mock(ShardingSphereSchema.class);
        when(GenericSchemaManager.getToBeDroppedTablesBySchemas(any(), any())).thenReturn(Collections.singletonMap("to_be_deleted", toBeDeletedSchemas));
        when(GenericSchemaManager.getToBeAddedTablesBySchemas(any(), any())).thenReturn(Collections.singletonMap("to_be_added", toBeAddedSchemas));
        metaDataPersistService.persistReloadDatabaseByAlter("foo_db", mock(ShardingSphereDatabase.class), mock(ShardingSphereDatabase.class));
        verify(databaseMetaDataService).persistByAlterConfiguration("foo_db", "to_be_added", toBeAddedSchemas);
        verify(databaseMetaDataService).delete("foo_db", "to_be_deleted", toBeDeletedSchemas);
    }
    
    @Test
    void assertPersistReloadDatabaseByDrop() {
        ShardingSphereSchema toBeDeletedSchemas = mock(ShardingSphereSchema.class);
        ShardingSphereSchema toBeAlterSchemas = mock(ShardingSphereSchema.class);
        when(GenericSchemaManager.getToBeDroppedTablesBySchemas(any(), any())).thenReturn(Collections.singletonMap("to_be_deleted", toBeDeletedSchemas));
        when(GenericSchemaManager.getToBeAddedTablesBySchemas(any(), any())).thenReturn(Collections.singletonMap("to_be_altered", toBeAlterSchemas));
        metaDataPersistService.persistReloadDatabaseByDrop("foo_db", mock(ShardingSphereDatabase.class), mock(ShardingSphereDatabase.class));
        verify(databaseMetaDataService).persistByDropConfiguration("foo_db", "to_be_altered", toBeAlterSchemas);
        verify(databaseMetaDataService).delete("foo_db", "to_be_deleted", toBeDeletedSchemas);
    }
}
