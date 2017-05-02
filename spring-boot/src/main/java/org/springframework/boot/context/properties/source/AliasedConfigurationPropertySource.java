/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.properties.source;

import java.util.Optional;

import org.springframework.util.Assert;

/**
 * A {@link ConfigurationPropertySource} supporting name aliases.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class AliasedConfigurationPropertySource implements ConfigurationPropertySource {

	private final ConfigurationPropertySource source;

	private final ConfigurationPropertyNameAliases aliases;

	AliasedConfigurationPropertySource(ConfigurationPropertySource source,
			ConfigurationPropertyNameAliases aliases) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(aliases, "Aliases must not be null");
		this.source = source;
		this.aliases = aliases;
	}

	@Override
	public ConfigurationProperty getConfigurationProperty(
			ConfigurationPropertyName name) {
		Assert.notNull(name, "Name must not be null");
		ConfigurationProperty result = getSource().getConfigurationProperty(name);
		if (result == null) {
			ConfigurationPropertyName aliasedName = getAliases().getNameForAlias(name);
			result = getSource().getConfigurationProperty(aliasedName);
		}
		return result;
	}

	@Override
	public Optional<Boolean> containsDescendantOf(ConfigurationPropertyName name) {
		Assert.notNull(name, "Name must not be null");
		Optional<Boolean> result = this.source.containsDescendantOf(name);
		for (ConfigurationPropertyName alias : getAliases().getAliases(name)) {
			Optional<Boolean> aliasResult = this.source.containsDescendantOf(alias);
			result = result.flatMap((r) -> aliasResult.flatMap(a -> Optional.of(r || a)));
		}
		return result;
	}

	protected ConfigurationPropertySource getSource() {
		return this.source;
	}

	protected ConfigurationPropertyNameAliases getAliases() {
		return this.aliases;
	}

}
