package com.example.calview.core.ai.di;

import com.example.calview.core.ai.FoodAnalysisService;
import com.google.ai.client.generativeai.GenerativeModel;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AiModule_ProvideFoodAnalysisServiceFactory implements Factory<FoodAnalysisService> {
  private final Provider<GenerativeModel> generativeModelProvider;

  public AiModule_ProvideFoodAnalysisServiceFactory(
      Provider<GenerativeModel> generativeModelProvider) {
    this.generativeModelProvider = generativeModelProvider;
  }

  @Override
  public FoodAnalysisService get() {
    return provideFoodAnalysisService(generativeModelProvider.get());
  }

  public static AiModule_ProvideFoodAnalysisServiceFactory create(
      Provider<GenerativeModel> generativeModelProvider) {
    return new AiModule_ProvideFoodAnalysisServiceFactory(generativeModelProvider);
  }

  public static FoodAnalysisService provideFoodAnalysisService(GenerativeModel generativeModel) {
    return Preconditions.checkNotNullFromProvides(AiModule.INSTANCE.provideFoodAnalysisService(generativeModel));
  }
}
