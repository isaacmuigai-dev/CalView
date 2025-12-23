package com.example.calview.core.data.di;

import com.example.calview.core.data.local.AppDatabase;
import com.example.calview.core.data.local.MealDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideMealDaoFactory implements Factory<MealDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideMealDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MealDao get() {
    return provideMealDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideMealDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideMealDaoFactory(databaseProvider);
  }

  public static MealDao provideMealDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMealDao(database));
  }
}
