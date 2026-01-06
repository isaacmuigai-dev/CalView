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

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<StorageRepository> storageRepositoryProvider;

  public MealRepositoryImpl_Factory(Provider<MealDao> mealDaoProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<StorageRepository> storageRepositoryProvider) {
    this.mealDaoProvider = mealDaoProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.storageRepositoryProvider = storageRepositoryProvider;
  }

  @Override
  public MealRepositoryImpl get() {
    return newInstance(mealDaoProvider.get(), firestoreRepositoryProvider.get(), authRepositoryProvider.get(), storageRepositoryProvider.get());
  }

  public static MealRepositoryImpl_Factory create(Provider<MealDao> mealDaoProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<StorageRepository> storageRepositoryProvider) {
    return new MealRepositoryImpl_Factory(mealDaoProvider, firestoreRepositoryProvider, authRepositoryProvider, storageRepositoryProvider);
  }

  public static MealRepositoryImpl newInstance(MealDao mealDao,
      FirestoreRepository firestoreRepository, AuthRepository authRepository,
      StorageRepository storageRepository) {
    return new MealRepositoryImpl(mealDao, firestoreRepository, authRepository, storageRepository);
  }
}
