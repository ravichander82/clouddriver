/*
 * Copyright 2022 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.netflix.spinnaker.clouddriver.google.config;

import com.netflix.spinnaker.clouddriver.google.ComputeVersion;
import com.netflix.spinnaker.clouddriver.google.security.GoogleNamedAccountCredentials;
import com.netflix.spinnaker.clouddriver.security.CredentialsInitializerSynchronizable;
import com.netflix.spinnaker.config.GoogleConfiguration;
import com.netflix.spinnaker.credentials.CredentialsTypeBaseConfiguration;
import com.netflix.spinnaker.credentials.CredentialsTypeProperties;
import com.netflix.spinnaker.credentials.definition.AbstractCredentialsLoader;
import com.netflix.spinnaker.credentials.poller.Poller;
import com.netflix.spinnaker.kork.configserver.ConfigFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCredentialsConfiguration {
  private static final Logger log = LoggerFactory.getLogger(GoogleCredentialsConfiguration.class);

  @Bean
  public CredentialsTypeBaseConfiguration<
          GoogleNamedAccountCredentials, GoogleConfigurationProperties.ManagedAccount>
      googleCredentialsProperties(
          ApplicationContext applicationContext,
          GoogleConfigurationProperties configurationProperties,
          ConfigFileService configFileService,
          GoogleConfiguration.DeployDefaults googleDeployDefaults,
          String clouddriverUserAgentApplicationName) {
    return new CredentialsTypeBaseConfiguration(
        applicationContext,
        CredentialsTypeProperties
            .<GoogleNamedAccountCredentials, GoogleConfigurationProperties.ManagedAccount>builder()
            .type(GoogleNamedAccountCredentials.CREDENTIALS_TYPE)
            .credentialsDefinitionClass(GoogleConfigurationProperties.ManagedAccount.class)
            .credentialsClass(GoogleNamedAccountCredentials.class)
            .credentialsParser(
                a -> {
                  try {
                    String jsonKey = configFileService.getContents(a.getJsonPath());

                    return new GoogleNamedAccountCredentials.Builder()
                        .jsonKey(jsonKey)
                        .project(a.getProject())
                        .applicationName(clouddriverUserAgentApplicationName)
                        .serviceAccountId(a.getServiceAccountId())
                        .serviceAccountProject(a.getServiceAccountProject())
                        .computeVersion(
                            a.isAlphaListed() ? ComputeVersion.ALPHA : ComputeVersion.DEFAULT)
                        .imageProjects(a.getImageProjects())
                        .liveLookupsEnabled(false)
                        .build();
                  } catch (Exception e) {
                    log.info("Error loading Google credentials: " + e.getMessage() + ".");
                    return null;
                  }
                })
            .defaultCredentialsSource(configurationProperties::getAccounts)
            .build());
  }

  @Bean
  public CredentialsInitializerSynchronizable googleCredentialsInitializerSynchronizable(
      AbstractCredentialsLoader<GoogleNamedAccountCredentials> loader) {
    final Poller<GoogleNamedAccountCredentials> poller = new Poller<>(loader);
    return new CredentialsInitializerSynchronizable() {
      @Override
      public void synchronize() {
        poller.run();
      }
    };
  }
}
