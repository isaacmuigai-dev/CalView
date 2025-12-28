package com.example.calview.core.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MealDao_Impl implements MealDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MealEntity> __insertionAdapterOfMealEntity;

  private final EntityDeletionOrUpdateAdapter<MealEntity> __deletionAdapterOfMealEntity;

  private final EntityDeletionOrUpdateAdapter<MealEntity> __updateAdapterOfMealEntity;

  public MealDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMealEntity = new EntityInsertionAdapter<MealEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `meals` (`id`,`name`,`calories`,`protein`,`carbs`,`fats`,`timestamp`,`imagePath`,`analysisStatus`,`analysisProgress`,`healthInsight`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MealEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCalories());
        statement.bindLong(4, entity.getProtein());
        statement.bindLong(5, entity.getCarbs());
        statement.bindLong(6, entity.getFats());
        statement.bindLong(7, entity.getTimestamp());
        if (entity.getImagePath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getImagePath());
        }
        statement.bindString(9, __AnalysisStatus_enumToString(entity.getAnalysisStatus()));
        statement.bindDouble(10, entity.getAnalysisProgress());
        if (entity.getHealthInsight() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getHealthInsight());
        }
      }
    };
    this.__deletionAdapterOfMealEntity = new EntityDeletionOrUpdateAdapter<MealEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `meals` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MealEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMealEntity = new EntityDeletionOrUpdateAdapter<MealEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `meals` SET `id` = ?,`name` = ?,`calories` = ?,`protein` = ?,`carbs` = ?,`fats` = ?,`timestamp` = ?,`imagePath` = ?,`analysisStatus` = ?,`analysisProgress` = ?,`healthInsight` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MealEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCalories());
        statement.bindLong(4, entity.getProtein());
        statement.bindLong(5, entity.getCarbs());
        statement.bindLong(6, entity.getFats());
        statement.bindLong(7, entity.getTimestamp());
        if (entity.getImagePath() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getImagePath());
        }
        statement.bindString(9, __AnalysisStatus_enumToString(entity.getAnalysisStatus()));
        statement.bindDouble(10, entity.getAnalysisProgress());
        if (entity.getHealthInsight() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getHealthInsight());
        }
        statement.bindLong(12, entity.getId());
      }
    };
  }

  @Override
  public Object insertMeal(final MealEntity meal, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMealEntity.insertAndReturnId(meal);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMeal(final MealEntity meal, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMealEntity.handle(meal);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMeal(final MealEntity meal, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMealEntity.handle(meal);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MealEntity>> getAllMeals() {
    final String _sql = "SELECT * FROM meals ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"meals"}, new Callable<List<MealEntity>>() {
      @Override
      @NonNull
      public List<MealEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFats = CursorUtil.getColumnIndexOrThrow(_cursor, "fats");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "imagePath");
          final int _cursorIndexOfAnalysisStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisStatus");
          final int _cursorIndexOfAnalysisProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisProgress");
          final int _cursorIndexOfHealthInsight = CursorUtil.getColumnIndexOrThrow(_cursor, "healthInsight");
          final List<MealEntity> _result = new ArrayList<MealEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MealEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFats;
            _tmpFats = _cursor.getInt(_cursorIndexOfFats);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImagePath;
            if (_cursor.isNull(_cursorIndexOfImagePath)) {
              _tmpImagePath = null;
            } else {
              _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
            }
            final AnalysisStatus _tmpAnalysisStatus;
            _tmpAnalysisStatus = __AnalysisStatus_stringToEnum(_cursor.getString(_cursorIndexOfAnalysisStatus));
            final float _tmpAnalysisProgress;
            _tmpAnalysisProgress = _cursor.getFloat(_cursorIndexOfAnalysisProgress);
            final String _tmpHealthInsight;
            if (_cursor.isNull(_cursorIndexOfHealthInsight)) {
              _tmpHealthInsight = null;
            } else {
              _tmpHealthInsight = _cursor.getString(_cursorIndexOfHealthInsight);
            }
            _item = new MealEntity(_tmpId,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFats,_tmpTimestamp,_tmpImagePath,_tmpAnalysisStatus,_tmpAnalysisProgress,_tmpHealthInsight);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MealEntity>> getMealsForDate(final long startOfDay, final long endOfDay) {
    final String _sql = "SELECT * FROM meals WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"meals"}, new Callable<List<MealEntity>>() {
      @Override
      @NonNull
      public List<MealEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFats = CursorUtil.getColumnIndexOrThrow(_cursor, "fats");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "imagePath");
          final int _cursorIndexOfAnalysisStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisStatus");
          final int _cursorIndexOfAnalysisProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisProgress");
          final int _cursorIndexOfHealthInsight = CursorUtil.getColumnIndexOrThrow(_cursor, "healthInsight");
          final List<MealEntity> _result = new ArrayList<MealEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MealEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFats;
            _tmpFats = _cursor.getInt(_cursorIndexOfFats);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImagePath;
            if (_cursor.isNull(_cursorIndexOfImagePath)) {
              _tmpImagePath = null;
            } else {
              _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
            }
            final AnalysisStatus _tmpAnalysisStatus;
            _tmpAnalysisStatus = __AnalysisStatus_stringToEnum(_cursor.getString(_cursorIndexOfAnalysisStatus));
            final float _tmpAnalysisProgress;
            _tmpAnalysisProgress = _cursor.getFloat(_cursorIndexOfAnalysisProgress);
            final String _tmpHealthInsight;
            if (_cursor.isNull(_cursorIndexOfHealthInsight)) {
              _tmpHealthInsight = null;
            } else {
              _tmpHealthInsight = _cursor.getString(_cursorIndexOfHealthInsight);
            }
            _item = new MealEntity(_tmpId,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFats,_tmpTimestamp,_tmpImagePath,_tmpAnalysisStatus,_tmpAnalysisProgress,_tmpHealthInsight);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<MealEntity>> getRecentUploads(final long startOfDay) {
    final String _sql = "SELECT * FROM meals WHERE timestamp >= ? ORDER BY timestamp DESC LIMIT 5";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfDay);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"meals"}, new Callable<List<MealEntity>>() {
      @Override
      @NonNull
      public List<MealEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFats = CursorUtil.getColumnIndexOrThrow(_cursor, "fats");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "imagePath");
          final int _cursorIndexOfAnalysisStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisStatus");
          final int _cursorIndexOfAnalysisProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisProgress");
          final int _cursorIndexOfHealthInsight = CursorUtil.getColumnIndexOrThrow(_cursor, "healthInsight");
          final List<MealEntity> _result = new ArrayList<MealEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MealEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFats;
            _tmpFats = _cursor.getInt(_cursorIndexOfFats);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImagePath;
            if (_cursor.isNull(_cursorIndexOfImagePath)) {
              _tmpImagePath = null;
            } else {
              _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
            }
            final AnalysisStatus _tmpAnalysisStatus;
            _tmpAnalysisStatus = __AnalysisStatus_stringToEnum(_cursor.getString(_cursorIndexOfAnalysisStatus));
            final float _tmpAnalysisProgress;
            _tmpAnalysisProgress = _cursor.getFloat(_cursorIndexOfAnalysisProgress);
            final String _tmpHealthInsight;
            if (_cursor.isNull(_cursorIndexOfHealthInsight)) {
              _tmpHealthInsight = null;
            } else {
              _tmpHealthInsight = _cursor.getString(_cursorIndexOfHealthInsight);
            }
            _item = new MealEntity(_tmpId,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFats,_tmpTimestamp,_tmpImagePath,_tmpAnalysisStatus,_tmpAnalysisProgress,_tmpHealthInsight);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMealById(final long id, final Continuation<? super MealEntity> $completion) {
    final String _sql = "SELECT * FROM meals WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MealEntity>() {
      @Override
      @Nullable
      public MealEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "calories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFats = CursorUtil.getColumnIndexOrThrow(_cursor, "fats");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "imagePath");
          final int _cursorIndexOfAnalysisStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisStatus");
          final int _cursorIndexOfAnalysisProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "analysisProgress");
          final int _cursorIndexOfHealthInsight = CursorUtil.getColumnIndexOrThrow(_cursor, "healthInsight");
          final MealEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpCalories;
            _tmpCalories = _cursor.getInt(_cursorIndexOfCalories);
            final int _tmpProtein;
            _tmpProtein = _cursor.getInt(_cursorIndexOfProtein);
            final int _tmpCarbs;
            _tmpCarbs = _cursor.getInt(_cursorIndexOfCarbs);
            final int _tmpFats;
            _tmpFats = _cursor.getInt(_cursorIndexOfFats);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpImagePath;
            if (_cursor.isNull(_cursorIndexOfImagePath)) {
              _tmpImagePath = null;
            } else {
              _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
            }
            final AnalysisStatus _tmpAnalysisStatus;
            _tmpAnalysisStatus = __AnalysisStatus_stringToEnum(_cursor.getString(_cursorIndexOfAnalysisStatus));
            final float _tmpAnalysisProgress;
            _tmpAnalysisProgress = _cursor.getFloat(_cursorIndexOfAnalysisProgress);
            final String _tmpHealthInsight;
            if (_cursor.isNull(_cursorIndexOfHealthInsight)) {
              _tmpHealthInsight = null;
            } else {
              _tmpHealthInsight = _cursor.getString(_cursorIndexOfHealthInsight);
            }
            _result = new MealEntity(_tmpId,_tmpName,_tmpCalories,_tmpProtein,_tmpCarbs,_tmpFats,_tmpTimestamp,_tmpImagePath,_tmpAnalysisStatus,_tmpAnalysisProgress,_tmpHealthInsight);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __AnalysisStatus_enumToString(@NonNull final AnalysisStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case ANALYZING: return "ANALYZING";
      case COMPLETED: return "COMPLETED";
      case FAILED: return "FAILED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private AnalysisStatus __AnalysisStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return AnalysisStatus.PENDING;
      case "ANALYZING": return AnalysisStatus.ANALYZING;
      case "COMPLETED": return AnalysisStatus.COMPLETED;
      case "FAILED": return AnalysisStatus.FAILED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
