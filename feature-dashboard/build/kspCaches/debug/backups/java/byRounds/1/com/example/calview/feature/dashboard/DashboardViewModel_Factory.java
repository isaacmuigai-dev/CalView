package com.example.calview.feature.dashboard;

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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<MealRepository> mealRepositoryProvider;

  public DashboardViewModel_Factory(Provider<MealRepository> mealRepositoryProvider) {
    this.mealRepositoryProvider = mealRepositoryProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(mealRepositoryProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<MealRepository> mealRepositoryProvider) {
    return new DashboardViewModel_Factory(mealRepositoryProvider);
  }

  public static DashboardViewModel newInstance(MealRepository mealRepository) {
    return new DashboardViewModel(mealRepository);
  }
}
