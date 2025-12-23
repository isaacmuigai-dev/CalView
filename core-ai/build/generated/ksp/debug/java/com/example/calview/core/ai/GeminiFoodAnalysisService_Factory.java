package com.example.calview.core.ai;

import com.google.ai.client.generativeai.GenerativeModel;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "deprecation"
})
public final class GeminiFoodAnalysisService_Factory implements Factory<GeminiFoodAnalysisService> {
  private final Provider<GenerativeModel> generativeModelProvider;

  public GeminiFoodAnalysisService_Factory(Provider<GenerativeModel> generativeModelProvider) {
    this.generativeModelProvider = generativeModelProvider;
  }

  @Override
  public GeminiFoodAnalysisService get() {
    return newInstance(generativeModelProvider.get());
  }

  public static GeminiFoodAnalysisService_Factory create(
      Provider<GenerativeModel> generativeModelProvider) {
    return new GeminiFoodAnalysisService_Factory(generativeModelProvider);
  }

  public static GeminiFoodAnalysisService newInstance(GenerativeModel generativeModel) {
    return new GeminiFoodAnalysisService(generativeModel);
  }
}
