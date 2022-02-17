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

package com.netflix.spinnaker.clouddriver.google.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spectator.api.Registry;
import com.netflix.spinnaker.clouddriver.google.provider.GoogleInfrastructureProvider;
import com.netflix.spinnaker.clouddriver.google.provider.agent.*;
import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleCredentialsLifecycleHandler
    implements CredentialsLifecycleHandler<GoogleNamedAccountCredentials> {

  private final GoogleInfrastructureProvider googleInfrastructureProvider;
  private final ObjectMapper objectMapper;
  private final Registry registry;

  @Override
  public void credentialsAdded(GoogleNamedAccountCredentials credentials) {
    addAgentFor(credentials);
  }

  @Override
  public void credentialsUpdated(GoogleNamedAccountCredentials credentials) {
    googleInfrastructureProvider.removeAgentsForAccounts(
        Collections.singleton(credentials.getName()));
    addAgentFor(credentials);
  }

  @Override
  public void credentialsDeleted(GoogleNamedAccountCredentials credentials) {
    googleInfrastructureProvider.removeAgentsForAccounts(
        Collections.singleton(credentials.getName()));
  }

  private void addAgentFor(GoogleNamedAccountCredentials credentials) {
    googleInfrastructureProvider.addAgents(
        List.of(
            new GoogleBackendServiceCachingAgent(
                credentials.getApplicationName(), credentials, objectMapper, registry),
            new GoogleGlobalAddressCachingAgent(
                credentials.getApplicationName(), credentials, objectMapper, registry),
            new GoogleHealthCheckCachingAgent(
                credentials.getApplicationName(), credentials, objectMapper, registry),
            new GoogleHttpHealthCheckCachingAgent(
                credentials.getApplicationName(), credentials, objectMapper, registry),
            /*  new GoogleHttpLoadBalancerCachingAgent(
            credentials.getApplicationName(), credentials, objectMapper, registry), */
            /*     new GoogleImageCachingAgent(
            credentials.getApplicationName(),
            credentials,
            objectMapper,
            registry,
            credentials.getImageProjects(),
            credentials.getImageProjects()), */
            new GoogleInstanceCachingAgent(),
            /*    new GoogleInternalLoadBalancerCachingAgent(
            credentials.getApplicationName(),
            credentials,
            objectMapper,
            registry,
            credentials.getRegions().toString()),  */
            new GoogleNetworkCachingAgent(),
            /*   new GoogleNetworkLoadBalancerCachingAgent(
            credentials.getApplicationName(),
            credentials,
            objectMapper,
            registry,
            credentials.getRegions().toString()),  */
            /*       new GoogleRegionalAddressCachingAgent(
            credentials.getApplicationName(),
            credentials,
            objectMapper,
            registry,
            credentials.getRegions().toString()),  */
            new GoogleSecurityGroupCachingAgent(
                credentials.getApplicationName(), credentials, objectMapper, registry),
            new GoogleSslCertificateCachingAgent(
                credentials.getApplicationName(), credentials, objectMapper, registry)));
    /*      new GoogleSslLoadBalancerCachingAgent(
    credentials.getApplicationName(), credentials, objectMapper, registry), */
    /*         new GoogleSubnetCachingAgent(
    credentials.getApplicationName(),
    credentials,
    objectMapper,
    registry,
    credentials.getRegions().toString())));  */
    /*      new GoogleTcpLoadBalancerCachingAgent(
    credentials.getApplicationName(), credentials, objectMapper, registry))); */
  }
}
