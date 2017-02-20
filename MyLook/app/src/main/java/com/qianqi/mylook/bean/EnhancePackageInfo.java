package com.qianqi.mylook.bean;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.qianqi.mylook.model.PackageModel;


/**
 * This can be compared to android.content.pm.EnhancePackageInfo (et al),
 * except that we are forced to parse the package manifest files
 * ourselves, so we don't use the classes in android.content.pm.
 */
public class EnhancePackageInfo implements Parcelable {
	public PackageInfo info;
	public String packageName;
	private String packageLabel;
	public Drawable icon;
	public boolean isQianqi;
	public boolean isPersistent;
	public boolean isSystem;
	public boolean hasActivity;
	public boolean allowAutoStart = false;
	public boolean isRunning = false;
	private boolean isStopping = false;
	private boolean inSmartList = false;
	private float usagePrediction = -1;
	private float usageQuickPrediction = -1;

	public EnhancePackageInfo(android.content.pm.PackageInfo packageInfo) {
		this.info = packageInfo;
		this.packageName = packageInfo.packageName;
		this.isQianqi = PackageModel.qianqiApps.contains(this.packageName);
	}

	@Override
    public int hashCode() {
		return this.packageName.hashCode();
	}

	/**
	 * Return a label identifying the package.
	 */
	public String getLabel() {
		if (packageLabel != null && !packageLabel.equals(""))
			return packageLabel;
		else
			return packageName;
	}

	public void setPackageLabel(String packageLabel) {
		this.packageLabel = packageLabel;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageName);
		dest.writeString(packageLabel);
		dest.writeInt(isSystem ? 1 : 0);
		dest.writeInt(inSmartList?1:0);
		dest.writeInt(isPersistent?1:0);
		dest.writeInt(hasActivity?1:0);
		// TODO: We don't currently parcel the icon. However,
		// due to how we use the ability to save to parcel at
		// this time (to remember the last selected receiver for an
		// in-progress toggle action), the icon isn't really needed.
		// Ultimately, we want a different fix for the whole
		// problem (see other TODOs).
	}

	public static final Creator<EnhancePackageInfo> CREATOR
	= new Creator<EnhancePackageInfo>()
	{
		public EnhancePackageInfo createFromParcel(Parcel in) {
			return new EnhancePackageInfo(in);
		}

		public EnhancePackageInfo[] newArray(int size) {
			return new EnhancePackageInfo[size];
		}
	};

	private EnhancePackageInfo(Parcel in) {
		packageName = in.readString();
		packageLabel = in.readString();
		isSystem = in.readInt() == 1;
		inSmartList = in.readInt() == 1;
		isPersistent = in.readInt() == 1;
		hasActivity = in.readInt() == 1;
	}

	public boolean isInSmartList() {
		return inSmartList;
	}

	public void setInSmartList(boolean inSmartList) {
		this.inSmartList = inSmartList;
	}

	public float getUsagePrediction() {
		return usagePrediction;
	}

	public void setUsagePrediction(float usagePrediction) {
		this.usagePrediction = usagePrediction;
	}

	public float getUsageQuickPrediction() {
		return usageQuickPrediction;
	}

	public void setUsageQuickPrediction(float usageQuickPrediction) {
		this.usageQuickPrediction = usageQuickPrediction;
	}

	public boolean isStopping() {
		return isStopping;
	}

	public void setStopping(boolean stopping) {
		isStopping = stopping;
	}
}