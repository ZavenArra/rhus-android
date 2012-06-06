package net.winterroot.android.rhus.provider;

import android.net.Uri;

public class RhusProject {
	public static final String AUTHORITY =
            "net.winterroot.android.rhus.provider.RhusProject";
	public static final String PROJECTS = "projects";

	
    public static final Uri PROJECTS_URI = Uri.parse("content://" +
            AUTHORITY + "/" + RhusProject.PROJECTS);
}
