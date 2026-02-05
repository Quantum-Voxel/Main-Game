package dev.ultreon.qvoxel.featureflags;

import java.util.Set;

public record FeatureSet(Set<Feature> enabledFeatures) {
    public static final FeatureSet NONE = new FeatureSet();
    public static final FeatureSet ALL = new FeatureSet(
            Features.IMPROVED_LIGHTING_SYSTEM
    );

    public FeatureSet(Feature... features) {
        this(Set.of(features));
    }

    public boolean isFeatureEnabled(Feature feature) {
        return enabledFeatures.contains(feature);
    }
}
