package org.totschnig.myexpenses.preference;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.model.ContribFeature;
import org.totschnig.myexpenses.util.Utils;

/**
 * Created by privat on 14.11.15.
 */
public class PasswordPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat
    implements TextWatcher, CompoundButton.OnCheckedChangeListener {

  private boolean boolProtectOrig, boolProtect, changePW = false;
  private String strPass1;
  private String strPass2;
  private EditText password1;
  private EditText password2;
  private TextInputLayout password2Wrapper;
  private CheckBox protect, change;
  private LinearLayout main, edit;

  @Override
  public void onDialogClosed(boolean positiveResult) {

    if (positiveResult) {
      if (boolProtect && strPass1 != null && strPass1.equals(strPass2)) {
        String hash = Utils.md5(strPass1);
        MyApplication.PrefKey.SET_PASSWORD.putString(hash);
      }
      ((PasswordPreference) getPreference()).setValue(boolProtect);
    }
  }

  //@Override
  protected void onBindDialogView(View view) {
    PasswordPreference preference = ((PasswordPreference) getPreference());
    password1 = (EditText) view.findViewById(R.id.password1);
    password2 = (EditText) view.findViewById(R.id.password2);
    protect = (CheckBox) view.findViewById(R.id.performProtection);
    change = (CheckBox) view.findViewById(R.id.changePassword);
    password2Wrapper = (TextInputLayout) view.findViewById(R.id.password2Wrapper);
    String warning = ContribFeature.SECURITY_QUESTION.hasAccess() ?
        getContext().getString(R.string.warning_password_contrib) :
        (getContext().getString(R.string.warning_password_no_contrib) + " " +
            ContribFeature.SECURITY_QUESTION.buildRequiresString(getContext()));
    ((TextView) view.findViewById(R.id.password_warning)).setText(warning);
    main = (LinearLayout) view.findViewById(R.id.layoutMain);
    edit = (LinearLayout) view.findViewById(R.id.layoutPasswordEdit);
    boolProtectOrig = preference.getValue();
    boolProtect = boolProtectOrig;
    protect.setChecked(boolProtect);
    if (boolProtect) {
      main.setVisibility(View.VISIBLE);
      view.findViewById(R.id.layoutChangePasswordCheckBox).setVisibility(View.VISIBLE);
      edit.setVisibility(View.GONE);
    }

    password1.addTextChangedListener(this);
    password2.addTextChangedListener(this);
    protect.setOnCheckedChangeListener(this);
    change.setOnCheckedChangeListener(this);
    super.onBindDialogView(view);
  }

  @Override
  public void afterTextChanged(Editable s) {
    validate();
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    switch (buttonView.getId()) {
      case R.id.performProtection:
        main.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        boolProtect = isChecked;
        validate();
        break;
      case R.id.changePassword:
        edit.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        changePW = isChecked;
        validate();
    }
  }

  private void validate() {
    Dialog dlg = getDialog();
    Button btn = ((AlertDialog) dlg).getButton(AlertDialog.BUTTON_POSITIVE);
    if (!boolProtect || (boolProtectOrig && !changePW)) {
      btn.setEnabled(true);
      return;
    }
    strPass1 = password1.getText().toString();
    strPass2 = password2.getText().toString();

    if (strPass1.equals("")) {
      btn.setEnabled(false);
    } else {
      if (strPass1.equals(strPass2)) {
        password2Wrapper.setError(null);
        btn.setEnabled(true);
      } else {
        if (!strPass2.equals("")) {
          password2Wrapper.setError(getString(R.string.pref_password_not_equal));
        }
        btn.setEnabled(false);
      }
    }
  }

  public static PasswordPreferenceDialogFragmentCompat newInstance(String key) {
    PasswordPreferenceDialogFragmentCompat fragment = new PasswordPreferenceDialogFragmentCompat();
    Bundle bundle = new Bundle(1);
    bundle.putString("key", key);
    fragment.setArguments(bundle);
    return fragment;
  }
}
