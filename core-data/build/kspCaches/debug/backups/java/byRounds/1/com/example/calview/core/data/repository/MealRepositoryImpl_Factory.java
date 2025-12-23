package com.example.calview.core.data.repository;

import com.example.calview.core.data.local.MealDao;
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
public final class MealRepositoryImpl_Factory implements Factory<MealRepositoryImpl> {
  private final Provider<MealDao> mealDaoProvider;

  public MealRepositoryImpl_Factory(Provider<MealDao> mealDaoProvider) {
    this.mealDaoProvider = mealDaoProvider;
  }

  @Override
  public MealRepositoryImpl get() {
    return newInstance(mealDaoProvider.get());
  }

  public static MealRepositoryImpl_Factory create(Provider<MealDao> mealDaoProvider) {
    return new MealRepositoryImpl_Factory(mealDaoProvider);
  }

  public static MealRepositoryImpl newInstance(MealDao mealDao) {
    return new MealRepositoryImpl(mealDao);
  }
}
