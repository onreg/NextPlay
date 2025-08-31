package io.github.onreg.feature.release.impl;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.onreg.data.release.api.ReleaseRepository;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class ReleasesViewModel_Factory implements Factory<ReleasesViewModel> {
  private final Provider<ReleaseRepository> releaseRepositoryProvider;

  private ReleasesViewModel_Factory(Provider<ReleaseRepository> releaseRepositoryProvider) {
    this.releaseRepositoryProvider = releaseRepositoryProvider;
  }

  @Override
  public ReleasesViewModel get() {
    return newInstance(releaseRepositoryProvider.get());
  }

  public static ReleasesViewModel_Factory create(
      Provider<ReleaseRepository> releaseRepositoryProvider) {
    return new ReleasesViewModel_Factory(releaseRepositoryProvider);
  }

  public static ReleasesViewModel newInstance(ReleaseRepository releaseRepository) {
    return new ReleasesViewModel(releaseRepository);
  }
}
