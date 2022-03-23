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
import com.netflix.spinnaker.clouddriver.names.NamerRegistry;
import com.netflix.spinnaker.config.GoogleConfiguration;
import com.netflix.spinnaker.credentials.definition.CredentialsParser;
import com.netflix.spinnaker.kork.configserver.ConfigFileService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleCredentialsParser
    implements CredentialsParser<
        GoogleConfigurationProperties.ManagedAccount, GoogleNamedAccountCredentials> {

  @Autowired ConfigFileService configFileService;

  String clouddriverUserAgentApplicationName;

  @Autowired GoogleConfiguration.DeployDefaults googleDeployDefaults;

  @Autowired GoogleConfigurationProperties configurationProperties;

  @Autowired NamerRegistry namerRegistry;

  @Nullable
  @Override
  public GoogleNamedAccountCredentials parse(
      GoogleConfigurationProperties.ManagedAccount credentials) {
    String jsonKey = configFileService.getContents(credentials.getJsonPath());

    return new GoogleNamedAccountCredentials.Builder()
        .name(credentials.getName())
        .environment(
            StringUtils.isEmpty(credentials.getEnvironment())
                ? credentials.getName()
                : credentials.getEnvironment())
        .accountType(
            StringUtils.isEmpty(credentials.getAccountType())
                ? credentials.getName()
                : credentials.getAccountType())
        .project(credentials.getProject())
        .computeVersion(credentials.isAlphaListed() ? ComputeVersion.ALPHA : ComputeVersion.DEFAULT)
        .jsonKey(jsonKey)
        .serviceAccountId(credentials.getServiceAccountId())
        .serviceAccountProject(credentials.getServiceAccountProject())
        .imageProjects(credentials.getImageProjects())
        .requiredGroupMembership(credentials.getRequiredGroupMembership())
        .permissions(credentials.getPermissions().build())
        .applicationName(clouddriverUserAgentApplicationName)
        .consulConfig(credentials.getConsul())
        .instanceTypeDisks(googleDeployDefaults.getInstanceTypeDisks())
        .userDataFile(credentials.getUserDataFile())
        .regionsToManage(credentials.getRegions(), configurationProperties.getDefaultRegions())
        .namer(namerRegistry.getNamingStrategy(credentials.getNamingStrategy()))
        .build();
  }
}
