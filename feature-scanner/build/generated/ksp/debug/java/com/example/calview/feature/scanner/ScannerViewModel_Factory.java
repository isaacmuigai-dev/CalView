package com.example.calview.feature.scanner;

import android.content.Context;
import com.example.calview.core.ai.FoodAnalysisService;
import com.example.calview.core.data.repository.MealRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ScannerViewModel_Factory implements Factory<ScannerViewModel> {
  private final Provider<FoodAnalysisService> foodAnalysisServiceProvider;

  private final Provider<MealRepository> mealRepositoryProvider;

  private final Provider<Context> contextProvider;

  public ScannerViewModel_Factory(Provider<FoodAnalysisService> foodAnalysisServiceProvider,
      Provider<MealRepository> mealRepositoryProvider, Provider<Context> contextProvider) {
    this.foodAnalysisServiceProvider = foodAnalysisServiceProvider;
    this.mealRepositoryProvider = mealRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ScannerViewModel get() {
    return newInstance(foodAnalysisServiceProvider.get(), mealRepositoryProvider.get(), contextProvider.get());
  }

  public static ScannerViewModel_Factory create(
      Provider<FoodAnalysisService> foodAnalysisServiceProvider,
      Provider<MealRepository> mealRepositoryProvider, Provider<Context> contextProvider) {
    return new ScannerViewModel_Factory(foodAnalysisServiceProvider, mealRepositoryProvider, contextProvider);
  }

  public static ScannerViewModel newInstance(FoodAnalysisService foodAnalysisService,
      MealRepository mealRepository, Context context) {
    return new ScannerViewModel(foodAnalysisService, mealRepository, context);
  }
}
