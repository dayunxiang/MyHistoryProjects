package com.mapuni.android.user;

import java.util.List;

import com.mapuni.android.task.TaskInfo;
import com.mapuni.android.user.userInterface.UserAuditInterface;
import com.mapuni.android.user.userInterface.UserCreateInterface;

/**
 * 分管领导类
 * 
 * @author Sahadev
 * 
 */
public class DeputyLeader extends BaseUser implements UserAuditInterface, UserCreateInterface {

	@Override
	public List<Object> getUserLeader() {
		return null;
	}

	@Override
	public List<TaskInfo> getUserTaskInfo() {
		return null;
	}

}
