/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package moe.shizuku.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.TypedArrayUtils;
import androidx.fragment.app.DialogFragment;

/**
 * A {@link Preference} that displays a list of entries as
 * a dialog.
 * <p>
 * This preference will store a set of strings into the SharedPreferences.
 * This set will contain one or more values from the
 * {@link #setEntryValues(CharSequence[])} array.
 *
 * @attr name android:entries
 * @attr name android:entryValues
 */
public class MultiSelectListPreference extends DialogPreference {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mValues = new HashSet<>();

    @SuppressLint("RestrictedApi")
    public MultiSelectListPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MultiSelectListPreference, defStyleAttr,
                defStyleRes);

        mEntries = TypedArrayUtils.getTextArray(a,
                R.styleable.MultiSelectListPreference_entries,
                R.styleable.MultiSelectListPreference_android_entries);

        mEntryValues = TypedArrayUtils.getTextArray(a,
                R.styleable.MultiSelectListPreference_entryValues,
                R.styleable.MultiSelectListPreference_android_entryValues);

        a.recycle();
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_Material);
    }

    public MultiSelectListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public MultiSelectListPreference(Context context) {
        this(context, null);
    }

    @NonNull
    @Override
    protected DialogFragment onCreateDialogFragment(String key) {
        final MultiSelectListPreferenceDialogFragment
                fragment = new MultiSelectListPreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(PreferenceDialogFragment.ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    /**
     * Sets the human-readable entries to be shown in the list. This will be
     * shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in
     * {@link #setEntryValues(CharSequence[])}.
     *
     * @param entries The entries.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }

    /**
     * @param entriesResId The entries array as a resource.
     * @see #setEntries(CharSequence[])
     */
    public void setEntries(@ArrayRes int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     *
     * @return The list as an array.
     */
    public CharSequence[] getEntries() {
        return mEntries;
    }

    /**
     * The array to find the value to save for a preference when an entry from
     * entries is selected. If a user clicks on the second item in entries, the
     * second item in this array will be saved to the preference.
     *
     * @param entryValues The array to be used as values to save for the preference.
     */
    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * @param entryValuesResId The entry values array as a resource.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    /**
     * Returns the array of values to be saved for the preference.
     *
     * @return The array of values.
     */
    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    /**
     * Sets the value of the key. This should contain entries in
     * {@link #getEntryValues()}.
     *
     * @param values The values to set for the key.
     */
    public void setValues(Set<String> values) {
        mValues.clear();
        mValues.addAll(values);

        persistStringSet(values);
    }

    /**
     * Retrieves the current value of the key.
     */
    public Set<String> getValues() {
        return mValues;
    }

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected boolean[] getSelectedItems() {
        final CharSequence[] entries = mEntryValues;
        final int entryCount = entries.length;
        final Set<String> values = mValues;
        boolean[] result = new boolean[entryCount];

        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }

        return result;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        final CharSequence[] defaultValues = a.getTextArray(index);
        final Set<String> result = new HashSet<>();

        for (final CharSequence defaultValue : defaultValues) {
            result.add(defaultValue.toString());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValues(restoreValue ? getPersistedStringSet(mValues) : (Set<String>) defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.values = getValues();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValues(myState.values);
    }

    private static class SavedState extends BaseSavedState {
        Set<String> values;

        public SavedState(Parcel source) {
            super(source);
            final int size = source.readInt();
            values = new HashSet<>();
            String[] strings = new String[size];
            source.readStringArray(strings);

            Collections.addAll(values, strings);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(values.size());
            dest.writeStringArray(values.toArray(new String[values.size()]));
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
