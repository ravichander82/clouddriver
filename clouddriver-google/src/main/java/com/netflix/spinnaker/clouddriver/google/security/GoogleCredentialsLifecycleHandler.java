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
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleCredentialsLifecycleHandler
    implements CredentialsLifecycleHandler<GoogleNamedAccountCredentials> {

  private final GoogleInfrastructureProvider googleInfrastructureProvider;
  private final ObjectMapper objectMapper;
  private final Registry registry;
  private final String clouddriverUserAgentApplicationName;

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

    //   List<String> regions = (List<String>)
    // credentials.getRegions().stream().findAny().get().keySet().stream().collect(Collectors.toList());
    // filter(x -> x.keySet().stream().collect(Collectors.toList())).collect(Collectors.toList());
    // */
    List<AbstractGoogleCachingAgent> googleCachingAgents = new LinkedList<>();
    googleCachingAgents.add(
        new GoogleSecurityGroupCachingAgent(
            clouddriverUserAgentApplicationName, credentials, objectMapper, registry));
    /*   googleCachingAgents.add(new GoogleNetworkCachingAgent(clouddriverUserAgentApplicationName,
    credentials,
    objectMapper,
    registry));  */
    googleCachingAgents.add(
        new GoogleGlobalAddressCachingAgent(
            clouddriverUserAgentApplicationName, credentials, objectMapper, registry));
    googleCachingAgents.add(
        new GoogleHealthCheckCachingAgent(
            clouddriverUserAgentApplicationName, credentials, objectMapper, registry));
    /*    googleCachingAgents.add(new GoogleSslLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
    credentials,
    objectMapper,
    registry));  */
    googleCachingAgents.add(
        new GoogleSslCertificateCachingAgent(
            clouddriverUserAgentApplicationName, credentials, objectMapper, registry));
    /*   googleCachingAgents.add(new GoogleTcpLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
    credentials,
    objectMapper,
    registry));  */
    googleCachingAgents.add(
        new GoogleBackendServiceCachingAgent(
            clouddriverUserAgentApplicationName, credentials, objectMapper, registry));
    /*   googleCachingAgents.add(new GoogleInstanceCachingAgent(clouddriverUserAgentApplicationName,
    credentials,
    objectMapper,
    registry));  */
    /*   googleCachingAgents.add(new GoogleImageCachingAgent(clouddriverUserAgentApplicationName,
    credentials,
    objectMapper,
    registry,
    credentials.getImageProjects(),
    googleConfigurationProperties.getBaseImageProjects()));  */
    /*  googleCachingAgents.add(new GoogleHttpLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
    credentials,
    objectMapper,
    registry)); */

    /*   for (String region : regions){
        googleCachingAgents.add(new GoogleSubnetCachingAgent(clouddriverUserAgentApplicationName,
          credentials,
          objectMapper,
          registry,
          region));
        googleCachingAgents.add(new GoogleRegionalAddressCachingAgent(clouddriverUserAgentApplicationName,
          credentials,
          objectMapper,
          registry,
          region));
    /*    googleCachingAgents.add(new GoogleInternalLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
          credentials,
          objectMapper,
          registry,
          region));  */
    /*    googleCachingAgents.add(new GoogleInternalHttpLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
          credentials,
          objectMapper,
          registry,
          region));
    /*    googleCachingAgents.add(new GoogleNetworkLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
          credentials,
          objectMapper,
          registry,
          region));  */
    /*    googleCachingAgents.add(new GoogleRegionalServerGroupCachingAgent(credentials,
      googleComputeApiFactory,
      registry,
      region,
      objectMapper));
    googleCachingAgents.add(new GoogleZonalServerGroupCachingAgent(credentials,
      googleComputeApiFactory,
      registry,
      region,
      objectMapper));   */
    // }

    googleInfrastructureProvider.addAgents(googleCachingAgents);
  }

  /*
  newlyAddedAgents << new GoogleSecurityGroupCachingAgent(clouddriverUserAgentApplicationName,
                                                                credentials,
                                                                objectMapper,
                                                                registry)
        newlyAddedAgents << new GoogleNetworkCachingAgent(clouddriverUserAgentApplicationName,
                                                          credentials,
                                                          objectMapper,
                                                          registry)

        newlyAddedAgents << new GoogleGlobalAddressCachingAgent(clouddriverUserAgentApplicationName,
                                                                credentials,
                                                                objectMapper,
                                                                registry)

        regions.each { String region ->
          newlyAddedAgents << new GoogleSubnetCachingAgent(clouddriverUserAgentApplicationName,
                                                           credentials,
                                                           objectMapper,
                                                           registry,
                                                           region)
          newlyAddedAgents << new GoogleRegionalAddressCachingAgent(clouddriverUserAgentApplicationName,
                                                                    credentials,
                                                                    objectMapper,
                                                                    registry,
                                                                    region)
        }

        newlyAddedAgents << new GoogleHealthCheckCachingAgent(clouddriverUserAgentApplicationName,
                                                              credentials,
                                                              objectMapper,
                                                              registry)
        newlyAddedAgents << new GoogleHttpHealthCheckCachingAgent(clouddriverUserAgentApplicationName,
                                                                  credentials,
                                                                  objectMapper,
                                                                  registry)
        newlyAddedAgents << new GoogleSslLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
                                                                  credentials,
                                                                  objectMapper,
                                                                  registry)
        newlyAddedAgents << new GoogleSslCertificateCachingAgent(clouddriverUserAgentApplicationName,
                                                                 credentials,
                                                                 objectMapper,
                                                                 registry)
        newlyAddedAgents << new GoogleTcpLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
                                                                  credentials,
                                                                  objectMapper,
                                                                  registry)
        newlyAddedAgents << new GoogleBackendServiceCachingAgent(clouddriverUserAgentApplicationName,
                                                                 credentials,
                                                                 objectMapper,
                                                                 registry)
        newlyAddedAgents << new GoogleInstanceCachingAgent(clouddriverUserAgentApplicationName,
                                                           credentials,
                                                           objectMapper,
                                                           registry)
        newlyAddedAgents << new GoogleImageCachingAgent(clouddriverUserAgentApplicationName,
                                                        credentials,
                                                        objectMapper,
                                                        registry,
                                                        credentials.imageProjects,
                                                        googleConfigurationProperties.baseImageProjects)
        newlyAddedAgents << new GoogleHttpLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
                                                                   credentials,
                                                                   objectMapper,
                                                                   registry)
        regions.each { String region ->
          newlyAddedAgents << new GoogleInternalLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
                                                                         credentials,
                                                                         objectMapper,
                                                                         registry,
                                                                         region)
          newlyAddedAgents << new GoogleInternalHttpLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
                                                                         credentials,
                                                                         objectMapper,
                                                                         registry,
                                                                         region)
          newlyAddedAgents << new GoogleNetworkLoadBalancerCachingAgent(clouddriverUserAgentApplicationName,
                                                                        credentials,
                                                                        objectMapper,
                                                                        registry,
                                                                        region)
          newlyAddedAgents << new GoogleRegionalServerGroupCachingAgent(credentials,
                                                                        computeApiFactory,
                                                                        registry,
                                                                        region,
                                                                        objectMapper)
          newlyAddedAgents << new GoogleZonalServerGroupCachingAgent(credentials,
                                                                     computeApiFactory,
                                                                     registry,
                                                                     region,
                                                                     objectMapper)
   */
}
