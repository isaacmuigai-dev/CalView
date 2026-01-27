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

  private final Provider<StreakFreezeRepository> streakFreezeRepositoryProvider;

  private final Provider<DailyLogRepository> dailyLogRepositoryProvider;

  private final Provider<GamificationRepository> gamificationRepositoryProvider;

  public MealRepositoryImpl_Factory(Provider<MealDao> mealDaoProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<StorageRepository> storageRepositoryProvider,
      Provider<SocialChallengeRepository> socialChallengeRepositoryProvider,
      Provider<StreakFreezeRepository> streakFreezeRepositoryProvider,
      Provider<DailyLogRepository> dailyLogRepositoryProvider,
      Provider<GamificationRepository> gamificationRepositoryProvider) {
    this.mealDaoProvider = mealDaoProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.storageRepositoryProvider = storageRepositoryProvider;
    this.socialChallengeRepositoryProvider = socialChallengeRepositoryProvider;
    this.streakFreezeRepositoryProvider = streakFreezeRepositoryProvider;
    this.dailyLogRepositoryProvider = dailyLogRepositoryProvider;
    this.gamificationRepositoryProvider = gamificationRepositoryProvider;
  }

  @Override
  public MealRepositoryImpl get() {
    return newInstance(mealDaoProvider.get(), firestoreRepositoryProvider.get(), authRepositoryProvider.get(), storageRepositoryProvider.get(), socialChallengeRepositoryProvider.get(), streakFreezeRepositoryProvider.get(), dailyLogRepositoryProvider.get(), gamificationRepositoryProvider.get());
  }

  public static MealRepositoryImpl_Factory create(Provider<MealDao> mealDaoProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<StorageRepository> storageRepositoryProvider,
      Provider<SocialChallengeRepository> socialChallengeRepositoryProvider,
      Provider<StreakFreezeRepository> streakFreezeRepositoryProvider,
      Provider<DailyLogRepository> dailyLogRepositoryProvider,
      Provider<GamificationRepository> gamificationRepositoryProvider) {
    return new MealRepositoryImpl_Factory(mealDaoProvider, firestoreRepositoryProvider, authRepositoryProvider, storageRepositoryProvider, socialChallengeRepositoryProvider, streakFreezeRepositoryProvider, dailyLogRepositoryProvider, gamificationRepositoryProvider);
  }

  public static MealRepositoryImpl newInstance(MealDao mealDao,
      FirestoreRepository firestoreRepository, AuthRepository authRepository,
      StorageRepository storageRepository, SocialChallengeRepository socialChallengeRepository,
      StreakFreezeRepository streakFreezeRepository, DailyLogRepository dailyLogRepository,
      GamificationRepository gamificationRepository) {
    return new MealRepositoryImpl(mealDao, firestoreRepository, authRepository, storageRepository, socialChallengeRepository, streakFreezeRepository, dailyLogRepository, gamificationRepository);
  }
}
