package com.example.calview.core.ai.di;

import com.google.ai.client.generativeai.GenerativeModel;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
    "deprecation"
})
public final class AiModule_ProvideGenerativeModelFactory implements Factory<GenerativeModel> {
  @Override
  public GenerativeModel get() {
    return provideGenerativeModel();
  }

  public static AiModule_ProvideGenerativeModelFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GenerativeModel provideGenerativeModel() {
    return Preconditions.checkNotNullFromProvides(AiModule.INSTANCE.provideGenerativeModel());
  }

  private static final class InstanceHolder {
    private static final AiModule_ProvideGenerativeModelFactory INSTANCE = new AiModule_ProvideGenerativeModelFactory();
  }
}
