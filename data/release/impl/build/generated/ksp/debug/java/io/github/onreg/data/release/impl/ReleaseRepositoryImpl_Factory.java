package io.github.onreg.data.release.impl;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ReleaseRepositoryImpl_Factory implements Factory<ReleaseRepositoryImpl> {
  @Override
  public ReleaseRepositoryImpl get() {
    return newInstance();
  }

  public static ReleaseRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ReleaseRepositoryImpl newInstance() {
    return new ReleaseRepositoryImpl();
  }

  private static final class InstanceHolder {
    static final ReleaseRepositoryImpl_Factory INSTANCE = new ReleaseRepositoryImpl_Factory();
  }
}
