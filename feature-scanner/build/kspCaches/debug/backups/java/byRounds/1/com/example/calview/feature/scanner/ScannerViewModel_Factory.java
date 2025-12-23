package com.example.calview.feature.scanner;

import com.example.calview.core.ai.FoodAnalysisService;
import com.example.calview.core.data.repository.MealRepository;
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
public final class ScannerViewModel_Factory implements Factory<ScannerViewModel> {
  private final Provider<FoodAnalysisService> foodAnalysisServiceProvider;

  private final Provider<MealRepository> mealRepositoryProvider;

  public ScannerViewModel_Factory(Provider<FoodAnalysisService> foodAnalysisServiceProvider,
      Provider<MealRepository> mealRepositoryProvider) {
    this.foodAnalysisServiceProvider = foodAnalysisServiceProvider;
    this.mealRepositoryProvider = mealRepositoryProvider;
  }

  @Override
  public ScannerViewModel get() {
    return newInstance(foodAnalysisServiceProvider.get(), mealRepositoryProvider.get());
  }

  public static ScannerViewModel_Factory create(
      Provider<FoodAnalysisService> foodAnalysisServiceProvider,
      Provider<MealRepository> mealRepositoryProvider) {
    return new ScannerViewModel_Factory(foodAnalysisServiceProvider, mealRepositoryProvider);
  }

  public static ScannerViewModel newInstance(FoodAnalysisService foodAnalysisService,
      MealRepository mealRepository) {
    return new ScannerViewModel(foodAnalysisService, mealRepository);
  }
}
