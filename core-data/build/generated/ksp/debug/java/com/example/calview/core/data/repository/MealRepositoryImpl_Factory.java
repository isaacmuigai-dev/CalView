package com.example.calview.core.data.repository;

import com.example.calview.core.data.local.MealDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MealRepositoryImpl_Factory implements Factory<MealRepositoryImpl> {
  private final Provider<MealDao> mealDaoProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<StorageRepository> storageRepositoryProvider;

  private final Provider<SocialChallengeRepository> socialChallengeRepositoryProvider;

  public MealRepositoryImpl_Factory(Provider<MealDao> mealDaoProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<StorageRepository> storageRepositoryProvider,
      Provider<SocialChallengeRepository> socialChallengeRepositoryProvider) {
    this.mealDaoProvider = mealDaoProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.storageRepositoryProvider = storageRepositoryProvider;
    this.socialChallengeRepositoryProvider = socialChallengeRepositoryProvider;
  }

  @Override
  public MealRepositoryImpl get() {
    return newInstance(mealDaoProvider.get(), firestoreRepositoryProvider.get(), authRepositoryProvider.get(), storageRepositoryProvider.get(), socialChallengeRepositoryProvider.get());
  }

  public static MealRepositoryImpl_Factory create(Provider<MealDao> mealDaoProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<StorageRepository> storageRepositoryProvider,
      Provider<SocialChallengeRepository> socialChallengeRepositoryProvider) {
    return new MealRepositoryImpl_Factory(mealDaoProvider, firestoreRepositoryProvider, authRepositoryProvider, storageRepositoryProvider, socialChallengeRepositoryProvider);
  }

  public static MealRepositoryImpl newInstance(MealDao mealDao,
      FirestoreRepository firestoreRepository, AuthRepository authRepository,
      StorageRepository storageRepository, SocialChallengeRepository socialChallengeRepository) {
    return new MealRepositoryImpl(mealDao, firestoreRepository, authRepository, storageRepository, socialChallengeRepository);
  }
}
