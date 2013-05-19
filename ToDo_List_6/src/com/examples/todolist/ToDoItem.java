package com.examples.todolist;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ToDoItem {

  long taskId;
  String task;
  Date created;
  
  public String getTask() {
    return task;
  }
  
  public Date getCreated() {
    return created;    
  }

  public ToDoItem(String _task, long _taskId) {
    this(_task, new Date(java.lang.System.currentTimeMillis()), _taskId);
  }
  
  public ToDoItem(String _task, Date _created, long _taskId) {
    taskId = _taskId;
	task = _task;
    created = _created;
  }

  @Override
  public String toString() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
    String dateString = sdf.format(created);  
    return "(" + dateString +  ") " + task; 
  }

	public long getTaskId() {
		return taskId;
	}

}