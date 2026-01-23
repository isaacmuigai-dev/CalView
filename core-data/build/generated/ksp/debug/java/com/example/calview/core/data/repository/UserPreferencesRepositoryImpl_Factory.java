package com.example.calview.core.data.repository;

import android.content.Context;
import com.example.calview.core.data.local.WeightHistoryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class UserPreferencesRepositoryImpl_Factory implements Factory<UserPreferencesRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<WeightHistoryDao> weightHistoryDaoProvider;

  public UserPreferencesRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<WeightHistoryDao> weightHistoryDaoProvider) {
    this.contextProvider = contextProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.weightHistoryDaoProvider = weightHistoryDaoProvider;
  }

  @Override
  public UserPreferencesRepositoryImpl get() {
    return newInstance(contextProvider.get(), authRepositoryProvider.get(), firestoreRepositoryProvider.get(), weightHistoryDaoProvider.get());
  }

  public static UserPreferencesRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<WeightHistoryDao> weightHistoryDaoProvider) {
    return new UserPreferencesRepositoryImpl_Factory(contextProvider, authRepositoryProvider, firestoreRepositoryProvider, weightHistoryDaoProvider);
  }

  public static UserPreferencesRepositoryImpl newInstance(Context context,
      AuthRepository authRepository, FirestoreRepository firestoreRepository,
      WeightHistoryDao weightHistoryDao) {
    return new UserPreferencesRepositoryImpl(context, authRepository, firestoreRepository, weightHistoryDao);
  }
}
