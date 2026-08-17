#ifndef COURSE_PLANNER_APP_H
#define COURSE_PLANNER_APP_H

#include "AVLTree.h"
#include "CourseLoader.h"
#include "Course.h"
#include <string>

/*
 * CoursePlannerApp.h
 * ----------------------------------------------------------------------------
 *	Public interface for the application layer. Owns the AVLTree and
 *	CourseLoader as members (replacing the original file-scope globals) and
 *	coordinates them through the menu loop. 
 *	
 *
 * Date: 07/20/2026
 * Author: Dylan P Harmnon
 */

class CoursePlannerApp {
public:
	void Run();

private:
	AVLTree			tree_;
	CourseLoader	loader_;

	void PrintMenu() const;
	void HandleLoad();
	void HandlePrintAll() const;
	void HandlePrintCourse() const;
	void PrintCourse(const Course& course) const;

	static std::string ToUpper(const std::string& s);
};


#endif // !COURSE_PLANNER_APP_H

