package com.example.calview.feature.dashboard;

import com.example.calview.core.data.health.HealthConnectManager;
import com.example.calview.core.data.repository.MealRepository;
import com.example.calview.core.data.repository.UserPreferencesRepository;
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

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private final Provider<HealthConnectManager> healthConnectManagerProvider;

  public DashboardViewModel_Factory(Provider<MealRepository> mealRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<HealthConnectManager> healthConnectManagerProvider) {
    this.mealRepositoryProvider = mealRepositoryProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
    this.healthConnectManagerProvider = healthConnectManagerProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(mealRepositoryProvider.get(), userPreferencesRepositoryProvider.get(), healthConnectManagerProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<MealRepository> mealRepositoryProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<HealthConnectManager> healthConnectManagerProvider) {
    return new DashboardViewModel_Factory(mealRepositoryProvider, userPreferencesRepositoryProvider, healthConnectManagerProvider);
  }

  public static DashboardViewModel newInstance(MealRepository mealRepository,
      UserPreferencesRepository userPreferencesRepository,
      HealthConnectManager healthConnectManager) {
    return new DashboardViewModel(mealRepository, userPreferencesRepository, healthConnectManager);
  }
}
