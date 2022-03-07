/*
 * Copyright 2015 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.config

import com.netflix.spinnaker.clouddriver.google.ComputeVersion
import com.netflix.spinnaker.clouddriver.google.GoogleCloudProvider
import com.netflix.spinnaker.clouddriver.google.GoogleExecutor
import com.netflix.spinnaker.clouddriver.google.compute.GoogleComputeApiFactory
import com.netflix.spinnaker.clouddriver.google.config.GoogleConfigurationProperties
import com.netflix.spinnaker.clouddriver.google.config.GoogleCredentialsConfiguration
import com.netflix.spinnaker.clouddriver.google.config.GoogleCredentialsDefinitionSource
import com.netflix.spinnaker.clouddriver.google.deploy.GoogleOperationPoller
import com.netflix.spinnaker.clouddriver.google.health.GoogleHealthIndicator
import com.netflix.spinnaker.clouddriver.google.model.GoogleDisk
import com.netflix.spinnaker.clouddriver.google.model.GoogleInstanceTypeDisk
import com.netflix.spinnaker.clouddriver.google.provider.GoogleInfrastructureProvider
import com.netflix.spinnaker.clouddriver.google.security.GoogleNamedAccountCredentials
import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable
import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler
import com.netflix.spinnaker.credentials.CredentialsRepository
import com.netflix.spinnaker.credentials.MapBackedCredentialsRepository
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader
import com.netflix.spinnaker.credentials.definition.BasicCredentialsLoader
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource
import com.netflix.spinnaker.credentials.poller.Poller
import com.netflix.spinnaker.kork.configserver.ConfigFileService
import groovy.transform.ToString
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.scheduling.annotation.EnableScheduling

import javax.annotation.Nullable

@Configuration
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty('google.enabled')
@ComponentScan(["com.netflix.spinnaker.clouddriver.google"])
@Import([ GoogleCredentialsConfiguration , GoogleCredentialsDefinitionSource ])
class GoogleConfiguration {

  private static final String DEFAULT_KEY = "default"

  @Bean
  @ConfigurationProperties("google")
  GoogleConfigurationProperties googleConfigurationProperties() {
    new GoogleConfigurationProperties()
  }

  @Bean
  GoogleHealthIndicator googleHealthIndicator() {
    new GoogleHealthIndicator()
  }

  @Bean
  GoogleOperationPoller googleOperationPoller() {
    new GoogleOperationPoller()
  }

  @Bean
  GoogleInfrastructureProvider googleInfrastructureProvider(){
    new GoogleInfrastructureProvider()
  }

  @Bean
  GoogleExecutor googleExecutor(){
    new GoogleExecutor()
  }

  @Bean
  static GoogleCredentialsDefinitionSource googleCredentialsDefinitionSource(){
    new GoogleCredentialsDefinitionSource()
  }

  @Bean
  @ConditionalOnMissingBean(
    value = GoogleNamedAccountCredentials.class,
    parameterizedContainer = CredentialsRepository.class)
  public CredentialsRepository<GoogleNamedAccountCredentials> googleCredentialsRepository(
    CredentialsLifecycleHandler<GoogleNamedAccountCredentials> eventHandler) {
    return new MapBackedCredentialsRepository<>(GoogleCloudProvider.ID, eventHandler)
  }

  @Bean
  @ConditionalOnMissingBean(
    value = GoogleNamedAccountCredentials.class,
    parameterizedContainer = AbstractCredentialsLoader.class)
  public AbstractCredentialsLoader<GoogleNamedAccountCredentials> googleCredentialsLoader(
    CredentialsDefinitionSource<GoogleConfigurationProperties.ManagedAccount> googleCredentialsSource,
    GoogleConfigurationProperties configurationProperties,
    CredentialsRepository<GoogleNamedAccountCredentials> googleCredentialsRepository,
    ConfigFileService configFileService) {

    if (googleCredentialsSource == null) {
     googleCredentialsSource = configurationProperties.getAccounts()
    }
    return new BasicCredentialsLoader<>(
      googleCredentialsSource,
      { managedAccount ->
        new GoogleNamedAccountCredentials.Builder()
          .name(managedAccount.name)
          .environment(managedAccount.environment ?: managedAccount.name)
          .accountType(managedAccount.accountType ?: managedAccount.name)
          .project(managedAccount.project)
          .computeVersion(managedAccount.alphaListed ? ComputeVersion.ALPHA : ComputeVersion.DEFAULT)
          .jsonKey(configFileService.getContents(managedAccount.jsonPath))
          .serviceAccountId(managedAccount.serviceAccountId)
          .serviceAccountProject(managedAccount.serviceAccountProject)
          .imageProjects(managedAccount.imageProjects)
          .requiredGroupMembership(managedAccount.requiredGroupMembership)
          .permissions(managedAccount.permissions.build())
        //     .applicationName(clouddriverUserAgentApplicationName)
          .consulConfig(managedAccount.consul)
        //     .instanceTypeDisks(googleDeployDefaults.instanceTypeDisks)
          .userDataFile(managedAccount.userDataFile)
        //     .regionsToManage(managedAccount.regions, googleConfigurationProperties.defaultRegions)
        //     .namer(namerRegistry.getNamingStrategy(managedAccount.namingStrategy))
          .build()
      },
      googleCredentialsRepository)
  }

  @Bean
  @ConditionalOnMissingBean(
    value = GoogleConfigurationProperties.ManagedAccount.class,
    parameterizedContainer = CredentialsDefinitionSource.class)
  public CredentialsInitializerSynchronizable googleCredentialsInitializerSynchronizable(
    AbstractCredentialsLoader<GoogleNamedAccountCredentials> loader) {
    final Poller<GoogleNamedAccountCredentials> poller = new Poller<>(loader)
    return new CredentialsInitializerSynchronizable() {
      @Override
      public void synchronize() {
        poller.run()
      }
    }
  }

  @Bean
  @ConfigurationProperties('google.defaults')
  DeployDefaults googleDeployDefaults() {
    new DeployDefaults()
  }

  @ToString(includeNames = true)
  static class DeployDefaults {
    List<GoogleDisk> fallbackInstanceTypeDisks = []
    List<GoogleInstanceTypeDisk> instanceTypeDisks = []

    GoogleInstanceTypeDisk determineInstanceTypeDisk(String instanceType) {
      GoogleInstanceTypeDisk instanceTypeDisk = instanceTypeDisks.find {
        it.instanceType == instanceType
      }

      if (!instanceTypeDisk) {
        instanceTypeDisk = instanceTypeDisks.find {
          it.instanceType == DEFAULT_KEY
        }
      }

      if (!instanceTypeDisk) {
        instanceTypeDisk = new GoogleInstanceTypeDisk(instanceType: DEFAULT_KEY,
                                                      disks: fallbackInstanceTypeDisks)
      }

      return instanceTypeDisk
    }
  }
}

