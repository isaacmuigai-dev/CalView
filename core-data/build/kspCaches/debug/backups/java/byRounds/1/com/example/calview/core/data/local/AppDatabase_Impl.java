package com.example.calview.core.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MealDao _mealDao;

  private volatile DailyLogDao _dailyLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `meals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firestoreId` TEXT NOT NULL, `name` TEXT NOT NULL, `calories` INTEGER NOT NULL, `protein` INTEGER NOT NULL, `carbs` INTEGER NOT NULL, `fats` INTEGER NOT NULL, `fiber` INTEGER NOT NULL, `sugar` INTEGER NOT NULL, `sodium` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `imagePath` TEXT, `imageUrl` TEXT, `analysisStatus` TEXT NOT NULL, `analysisProgress` REAL NOT NULL, `healthInsight` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `daily_logs` (`date` TEXT NOT NULL, `steps` INTEGER NOT NULL, `waterIntake` INTEGER NOT NULL, `weight` REAL NOT NULL, `caloriesConsumed` INTEGER NOT NULL, `proteinConsumed` INTEGER NOT NULL, `carbsConsumed` INTEGER NOT NULL, `fatsConsumed` INTEGER NOT NULL, `fiberConsumed` INTEGER NOT NULL, `sugarConsumed` INTEGER NOT NULL, `sodiumConsumed` INTEGER NOT NULL, `caloriesBurned` INTEGER NOT NULL, `exerciseMinutes` INTEGER NOT NULL, `firestoreId` TEXT NOT NULL, PRIMARY KEY(`date`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b2dfe816182380b3d246974a9f3306e2')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `meals`");
        db.execSQL("DROP TABLE IF EXISTS `daily_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMeals = new HashMap<String, TableInfo.Column>(16);
        _columnsMeals.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("firestoreId", new TableInfo.Column("firestoreId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("calories", new TableInfo.Column("calories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("protein", new TableInfo.Column("protein", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("carbs", new TableInfo.Column("carbs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("fats", new TableInfo.Column("fats", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("fiber", new TableInfo.Column("fiber", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("sugar", new TableInfo.Column("sugar", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("sodium", new TableInfo.Column("sodium", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("imagePath", new TableInfo.Column("imagePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("imageUrl", new TableInfo.Column("imageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("analysisStatus", new TableInfo.Column("analysisStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("analysisProgress", new TableInfo.Column("analysisProgress", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeals.put("healthInsight", new TableInfo.Column("healthInsight", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMeals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMeals = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMeals = new TableInfo("meals", _columnsMeals, _foreignKeysMeals, _indicesMeals);
        final TableInfo _existingMeals = TableInfo.read(db, "meals");
        if (!_infoMeals.equals(_existingMeals)) {
          return new RoomOpenHelper.ValidationResult(false, "meals(com.example.calview.core.data.local.MealEntity).\n"
                  + " Expected:\n" + _infoMeals + "\n"
                  + " Found:\n" + _existingMeals);
        }
        final HashMap<String, TableInfo.Column> _columnsDailyLogs = new HashMap<String, TableInfo.Column>(14);
        _columnsDailyLogs.put("date", new TableInfo.Column("date", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("steps", new TableInfo.Column("steps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("waterIntake", new TableInfo.Column("waterIntake", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("weight", new TableInfo.Column("weight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("caloriesConsumed", new TableInfo.Column("caloriesConsumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("proteinConsumed", new TableInfo.Column("proteinConsumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("carbsConsumed", new TableInfo.Column("carbsConsumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("fatsConsumed", new TableInfo.Column("fatsConsumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("fiberConsumed", new TableInfo.Column("fiberConsumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("sugarConsumed", new TableInfo.Column("sugarConsumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("sodiumConsumed", new TableInfo.Column("sodiumConsumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("caloriesBurned", new TableInfo.Column("caloriesBurned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("exerciseMinutes", new TableInfo.Column("exerciseMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDailyLogs.put("firestoreId", new TableInfo.Column("firestoreId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDailyLogs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDailyLogs = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoDailyLogs = new TableInfo("daily_logs", _columnsDailyLogs, _foreignKeysDailyLogs, _indicesDailyLogs);
        final TableInfo _existingDailyLogs = TableInfo.read(db, "daily_logs");
        if (!_infoDailyLogs.equals(_existingDailyLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "daily_logs(com.example.calview.core.data.local.DailyLogEntity).\n"
                  + " Expected:\n" + _infoDailyLogs + "\n"
                  + " Found:\n" + _existingDailyLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b2dfe816182380b3d246974a9f3306e2", "0969485336cf4f90bd599e4d13b86c1b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "meals","daily_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `meals`");
      _db.execSQL("DELETE FROM `daily_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MealDao.class, MealDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DailyLogDao.class, DailyLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MealDao mealDao() {
    if (_mealDao != null) {
      return _mealDao;
    } else {
      synchronized(this) {
        if(_mealDao == null) {
          _mealDao = new MealDao_Impl(this);
        }
        return _mealDao;
      }
    }
  }

  @Override
  public DailyLogDao dailyLogDao() {
    if (_dailyLogDao != null) {
      return _dailyLogDao;
    } else {
      synchronized(this) {
        if(_dailyLogDao == null) {
          _dailyLogDao = new DailyLogDao_Impl(this);
        }
        return _dailyLogDao;
      }
    }
  }
}
