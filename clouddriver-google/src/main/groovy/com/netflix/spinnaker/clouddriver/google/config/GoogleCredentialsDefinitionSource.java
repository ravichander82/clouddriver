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

import com.google.common.collect.ImmutableList;
import com.netflix.spinnaker.credentials.definition.CredentialsDefinitionSource;
import java.util.ArrayList;
import java.util.List;

public class GoogleCredentialsDefinitionSource
    implements CredentialsDefinitionSource<GoogleConfigurationProperties.ManagedAccount> {

  GoogleCredentialsDefinitionSource() {}

  @Override
  public List<GoogleConfigurationProperties.ManagedAccount> getCredentialsDefinitions() {
    List<GoogleConfigurationProperties.ManagedAccount> googleCredentialsDefinitions =
        new ArrayList<>();
    /*
    GoogleConfigurationProperties.ManagedAccount managedAccount1 =
        new GoogleConfigurationProperties.ManagedAccount();
    managedAccount1.setProject("main-host-project-162535");
    managedAccount1.setName("gce-account");
    managedAccount1.setJsonPath(
        "/home/ravichander/.gcp/main-host-project-162535-053a1c5d6cd2.json");

    GoogleConfigurationProperties.ManagedAccount managedAccount2 =
        new GoogleConfigurationProperties.ManagedAccount();
    managedAccount2.setProject("service-project-326811");
    managedAccount2.setName("gce-service-account");
    managedAccount2.setJsonPath("/home/ravichander/.gcp/service-project-326811-0be6357415ba.json");

    googleCredentialsDefinitions.add(managedAccount1);
    googleCredentialsDefinitions.add(managedAccount2);  */

    return ImmutableList.copyOf(googleCredentialsDefinitions);
  }
}
