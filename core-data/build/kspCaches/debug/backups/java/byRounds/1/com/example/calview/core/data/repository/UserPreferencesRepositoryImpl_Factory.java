package com.example.calview.core.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "deprecation"
})
public final class UserPreferencesRepositoryImpl_Factory implements Factory<UserPreferencesRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  public UserPreferencesRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public UserPreferencesRepositoryImpl get() {
    return newInstance(contextProvider.get(), authRepositoryProvider.get(), firestoreRepositoryProvider.get());
  }

  public static UserPreferencesRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new UserPreferencesRepositoryImpl_Factory(contextProvider, authRepositoryProvider, firestoreRepositoryProvider);
  }

  public static UserPreferencesRepositoryImpl newInstance(Context context,
      AuthRepository authRepository, FirestoreRepository firestoreRepository) {
    return new UserPreferencesRepositoryImpl(context, authRepository, firestoreRepository);
  }
}
