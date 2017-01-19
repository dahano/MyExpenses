package org.totschnig.myexpenses.test.util;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import junit.framework.Assert;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.activity.MyExpenses;
import org.totschnig.myexpenses.model.*;
import org.totschnig.myexpenses.model.AccountType;
import org.totschnig.myexpenses.model.Transaction.CrStatus;
//import org.totschnig.myexpenses.test.R;
import org.totschnig.myexpenses.util.CategoryTree;
import org.totschnig.myexpenses.util.Result;
import org.totschnig.myexpenses.util.Utils;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

@SuppressLint("InlinedApi")
public class Fixture {
  private static Account account1;
  private static Account account2;
  private static Account account3;
  private static Currency foreignCurrency;

  private Fixture() {
  }

  public static Account getAccount1() {
    return account1;
  }
  public static Account getAccount2() {
    return account2;
  }
  public static Account getAccount3() {
    return account3;
  }
  public static Account getAccount4() {
    return account4;
  }
  public static Currency getForeignCurrency() {
    return foreignCurrency;
  }
  private static Account account4;
  @SuppressLint("NewApi")

  //If provided a file will delete it. 
  //If provided a directory will recursively delete files but preserve directories
  private static void delete(File file_or_directory) {
      if (file_or_directory == null) {
          return;
      }

      if (file_or_directory.isDirectory()) {
          if (file_or_directory.listFiles() != null) {
              for(File f : file_or_directory.listFiles()) {
                  delete(f);
              }
          }
      } else {
          file_or_directory.delete();
      }
  }
  public static void setup(Instrumentation inst, Locale locale, Currency defaultCurrency) {
    setup(inst,locale,defaultCurrency,-1);
  }
  public static void setup(Instrumentation inst, Locale locale, Currency defaultCurrency,int stage) {
    Context testContext = inst.getContext();
    Context appContext = inst.getTargetContext().getApplicationContext();
    foreignCurrency = Currency.getInstance(testContext.getString(R.string.testData_account2Currency));

    account1 = Account.getInstanceFromDb(0);
    account1.currency = defaultCurrency;
    account1.description = testContext.getString(R.string.testData_account1Description);
    account1.label = testContext.getString(R.string.testData_account1Label);
    account1.openingBalance = new Money(defaultCurrency,2000L);
    account1.grouping = Grouping.DAY;
    account1.save();
    if (stage ==1) return;
    account2 = new Account(
        testContext.getString(R.string.testData_account2Label),
        foreignCurrency,
        50000,
        testContext.getString(R.string.testData_account2Description), AccountType.CASH,
        Build.VERSION.SDK_INT > 13 ? appContext.getResources().getColor(org.totschnig.myexpenses.R.color.material_red) : Color.RED
    );
    account2.save();
    if (stage ==2) return;
    account3 = new Account(
        testContext.getString(R.string.testData_account3Label),
        defaultCurrency,
        200000,
        testContext.getString(R.string.testData_account3Description), AccountType.BANK,
        Build.VERSION.SDK_INT > 13 ? appContext.getResources().getColor(org.totschnig.myexpenses.R.color.material_blue) : Color.BLUE
    );
    account3.grouping = Grouping.DAY;
    account3.save();
    account4 = new Account(
        testContext.getString(R.string.testData_account3Description),
        foreignCurrency,
        0,
        "",
        AccountType.CCARD,
        Build.VERSION.SDK_INT > 13 ? appContext.getResources().getColor(org.totschnig.myexpenses.R.color.material_cyan) : Color.CYAN);
    account4.save();
    //set up categories
    setUpCategories(locale, appContext);
    //set up transactions
    long now = System.currentTimeMillis();
    //are used twice
    long mainCat1 = findCat(testContext.getString(R.string.testData_transaction1MainCat), null);
    long mainCat2 = findCat(testContext.getString(R.string.testData_transaction2MainCat), null);
    long mainCat6 = findCat(testContext.getString(R.string.testData_transaction6MainCat), null);

    //Transaction 1
    Transaction op1 = Transaction.getNewInstance(account3.getId());
    op1.setAmount(new Money(defaultCurrency,-1200L));
    op1.setCatId(findCat(testContext.getString(R.string.testData_transaction1SubCat), mainCat1));
    op1.setDate(new Date(now - 300000));
    op1.setPictureUri(Uri.fromFile(new File(appContext.getExternalFilesDir(null), "screenshot.jpg")));
    op1.save();

    //Transaction 2
    Transaction op2 = Transaction.getNewInstance(account3.getId());
    op2.setAmount(new Money(defaultCurrency,-2200L));
    op2.setCatId(findCat(testContext.getString(R.string.testData_transaction2SubCat), mainCat2));
    op2.comment = testContext.getString(R.string.testData_transaction2Comment);
    op2.setDate(new Date( now - 7200000 ));
    op2.save();
    Transaction op3 = Transaction.getNewInstance(account3.getId());

    //Transaction 3 Cleared
    op3.setAmount(new Money(defaultCurrency,-2500L));
    op3.setCatId(findCat(testContext.getString(R.string.testData_transaction3SubCat),
        findCat(testContext.getString(R.string.testData_transaction3MainCat), null)));
    op3.setDate(new Date( now - 72230000 ));
    op3.crStatus = CrStatus.CLEARED;
    op3.save();

    //Transaction 4 Cleared
    Transaction op4 = Transaction.getNewInstance(account3.getId());
    op4.setAmount(new Money(defaultCurrency,-5000L));
    op4.setCatId(findCat(testContext.getString(R.string.testData_transaction4SubCat), mainCat2));
    op4.payee = testContext.getString(R.string.testData_transaction4Payee);
    op4.setDate(new Date( now - 98030000 ));
    op4.crStatus = CrStatus.CLEARED;
    op4.save();

    //Transaction 5 Reconciled
    Transaction op5 = Transfer.getNewInstance(account1.getId(),account3.getId());
    op5.setAmount(new Money(defaultCurrency,-10000L));
    op5.setDate(new Date( now - 800390000 ));
    op5.crStatus = CrStatus.RECONCILED;
    op5.save();

    //Transaction 6 Gift Reconciled
    Transaction op6 = Transaction.getNewInstance(account3.getId());
    op6.setAmount(new Money(defaultCurrency,10000L));
    op6.setCatId(mainCat6);
    op6.setDate(new Date( now - 810390000 ));
    op6.crStatus = CrStatus.RECONCILED;
    op6.save();

    //Transaction 7 Second account foreign Currency
    Transaction op7 = Transaction.getNewInstance(account2.getId());
    op7.setAmount(new Money(foreignCurrency,-34523L));
    op7.setDate(new Date( now - 1003900000 ));
    op7.save();

    //Transaction 8: Split
    Transaction op8 = SplitTransaction.getNewInstance(account3.getId());
    op8.setAmount(new Money(defaultCurrency,-8967L));
    op8.save();
    Transaction split1 = SplitPartCategory.getNewInstance(account3.getId(),op8.getId());
    split1.setAmount(new Money(defaultCurrency,-4523L));
    split1.setCatId(mainCat2);
    split1.save();
    Transaction split2 = SplitPartCategory.getNewInstance(account3.getId(),op8.getId());
    split2.setAmount(new Money(defaultCurrency,-4444L));
    split2.setCatId(mainCat6);
    split2.save();

    // Template
    Assert.assertNotSame("Unable to create planner", MyApplication.getInstance().createPlanner(true),MyApplication.INVALID_CALENDAR_ID);
    //createPlanner sets up a new plan, mPlannerCalendarId is only set in onSharedPreferenceChanged
    //if it is has not been called yet, when we save our plan, saving fails.
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Template template = Template.getTypedNewInstance(MyExpenses.TYPE_TRANSACTION, account3.getId());
    template.setAmount(new Money(defaultCurrency,-90000L));
    String templateSubCat = testContext.getString(R.string.testData_templateSubCat);
    template.setCatId(findCat(templateSubCat,
        findCat(testContext.getString(R.string.testData_templateMainCat), null)));
    template.setTitle(templateSubCat);
    template.payee = testContext.getString(R.string.testData_templatePayee);
    Uri planUri = new Plan(
        Calendar.getInstance(),
        "FREQ=WEEKLY;COUNT=10;WKST=SU",
        template.getTitle(),
        template.compileDescription(appContext))
      .save();
    template.planId = ContentUris.parseId(planUri);
    Uri templateuri = template.save();
    if (templateuri == null)
      throw new RuntimeException("Could not save template");
  }

  public static void setUpCategories(Locale locale, Context appContext) {
    int sourceRes = appContext.getResources().getIdentifier("cat_"+locale.getLanguage(), "raw", appContext.getPackageName());
    InputStream catXML;
    try {
      catXML = appContext.getResources().openRawResource(sourceRes);
    } catch (NotFoundException e) {
      catXML = appContext.getResources().openRawResource(org.totschnig.myexpenses.R.raw.cat_en);
    }

    Result result = Utils.analyzeGrisbiFileWithSAX(catXML);
    Utils.importCats((CategoryTree) result.extra[0], null);
  }

  public static long findCat(String label, Long parent) {
   Long result = Category.find(label,parent);
   if (result == -1) {
     throw new RuntimeException("Could not find category");
   }
   return result;
  }
}
