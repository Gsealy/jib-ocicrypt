/*
 * Copyright 2017 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.image.json;

import com.google.cloud.tools.jib.api.DescriptorDigest;
import com.google.cloud.tools.jib.image.json.BuildableManifestTemplate.ContentDescriptorTemplate;
import com.google.cloud.tools.jib.json.JsonTemplateMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link V22ManifestTemplate}. */
public class V22ManifestTemplateTest {

  @Test
  public void testToJson() throws DigestException, IOException, URISyntaxException {
    // Loads the expected JSON string.
    Path jsonFile = Paths.get(Resources.getResource("core/json/v22manifest.json").toURI());
    String expectedJson = new String(Files.readAllBytes(jsonFile), StandardCharsets.UTF_8);

    // Creates the JSON object to serialize.
    V22ManifestTemplate manifestJson = new V22ManifestTemplate();

    manifestJson.setContainerConfiguration(
        1000,
        DescriptorDigest.fromDigest(
            "sha256:8c662931926fa990b41da3c9f42663a537ccd498130030f9149173a0493832ad"));

    manifestJson.addLayer(
        1000_000,
        DescriptorDigest.fromHash(
            "4945ba5011739b0b98c4a41afe224e417f47c7c99b2ce76830999c9a0861b236"),
        null);

    // Serializes the JSON object.
    Assert.assertEquals(expectedJson, JsonTemplateMapper.toUtf8String(manifestJson));
  }

  @Test
  public void testFromJson() throws IOException, URISyntaxException, DigestException {
    // Loads the JSON string.
    Path jsonFile = Paths.get(Resources.getResource("core/json/v22manifest.json").toURI());

    // Deserializes into a manifest JSON object.
    V22ManifestTemplate manifestJson =
        JsonTemplateMapper.readJsonFromFile(jsonFile, V22ManifestTemplate.class);

    Assert.assertEquals(
        DescriptorDigest.fromDigest(
            "sha256:8c662931926fa990b41da3c9f42663a537ccd498130030f9149173a0493832ad"),
        manifestJson.getContainerConfiguration().getDigest());

    Assert.assertEquals(1000, manifestJson.getContainerConfiguration().getSize());

    Assert.assertEquals(
        DescriptorDigest.fromHash(
            "4945ba5011739b0b98c4a41afe224e417f47c7c99b2ce76830999c9a0861b236"),
        manifestJson.getLayers().get(0).getDigest());

    Assert.assertEquals(1000_000, manifestJson.getLayers().get(0).getSize());
  }

  @Test
  public void testFromJson_optionalProperties() throws IOException, URISyntaxException {
    Path jsonFile =
        Paths.get(Resources.getResource("core/json/v22manifest_optional_properties.json").toURI());

    V22ManifestTemplate manifestJson =
        JsonTemplateMapper.readJsonFromFile(jsonFile, V22ManifestTemplate.class);

    List<ContentDescriptorTemplate> layers = manifestJson.getLayers();
    Assert.assertEquals(4, layers.size());
    Assert.assertNull(layers.get(0).getUrls());
    Assert.assertNull(layers.get(0).getAnnotations());

    Assert.assertEquals(Arrays.asList("url-foo", "url-bar"), layers.get(1).getUrls());
    Assert.assertNull(layers.get(1).getAnnotations());

    Assert.assertNull(layers.get(2).getUrls());
    Assert.assertEquals(ImmutableMap.of("key-foo", "value-foo"), layers.get(2).getAnnotations());

    Assert.assertEquals(Arrays.asList("cool-url"), layers.get(3).getUrls());
    Assert.assertEquals(
        ImmutableMap.of("key1", "value1", "key2", "value2"), layers.get(3).getAnnotations());
  }
}
