//============================================================================
// Name        : ProjectTwo.cpp
// Author      : Dylan Harmon
// Version     : 1.0
// Date        : 8/17/2025
//============================================================================

#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <string>
#include <algorithm>

using namespace std;

// ------------- Structures ----------------

// Structure to hold course information
struct Course {
	string courseID;
	string courseName;
	vector<string> prerequisites;
};

// Internal structure for tree node
struct Node {
	Course course;
	Node* left;
	Node* right;

	Node(Course c) : course(c), left(nullptr), right(nullptr) {}
};

// ------------- Binary Search Tree Class ----------------
class BinarySearchTree {
private:
	Node* root;

	/*
	*  Inserts a course into the binary search tree.
	* 
	* @param node: The current node in the BST (root initially)
	* @param course: The course to be inserted
	*/
	Node* Insert(Node* node, Course course) {
		if (!node)
			return new Node(course);

		if (course.courseID < node->course.courseID)
			node->left = Insert(node->left, course);
		else if (course.courseID > node->course.courseID)
			node->right = Insert(node->right, course);

		return node; // Duplicate IDs are not allowed
	}

	/*
	* IN-Order traversal of the binary search tree.
	* 
	* @param node: The current node in the BST (root initially)
	*/
	void InOrder(Node* node) {
		if (!node) return;
		InOrder(node->left);
		cout << node->course.courseID << ", " << node->course.courseName << endl;
		InOrder(node->right);
	}

	/*
	*  Searches for a course by its ID in the binary search tree.
	* 
	*  @param node: The current node in the BST (root initially)
	*/
	Course* Search(Node* node, const string& courseID) {
		if (!node) return nullptr;
		if (courseID == node->course.courseID)
			return &node->course;
		else if (courseID < node->course.courseID)
			return Search(node->left, courseID);
		else
			return Search(node->right, courseID);
	}

	// recursively deletes all nodes in the tree
	void DeleteTree(Node* node) {
		if (!node) return;
		
		DeleteTree(node->left);
		DeleteTree(node->right);
		delete node;
		
	}

public:

	// Constructor to initialize the root of the BST
	BinarySearchTree() : root(nullptr) {}

	// Destructor to clean up memory
	~BinarySearchTree() {
		while (root != nullptr) {
			DeleteTree(root);
			root = nullptr;
		}
	}

	/*
	*  Inserts a course into the binary search tree.
	*/
	void InsertCource(Course course) {
		root = Insert(root, course);
	}

	/*
	*  Recursively traverses the tree in-order and prints all courses.
	*/
	void PrintAllCourses() {
		InOrder(root);
	}

	/*
	*  @param courseID: The ID of the course to search for
	*/
	void PrintCourseInfo(const string& courseID) {
		Course* course = Search(root, courseID);
		if (!course) {
			cout << "Error: Course not found." << endl;
			return;
		}

		cout << "Course: " << course->courseID << " - " << course->courseName << endl;

		if (course->prerequisites.empty()) {
			cout << "Prerequisites: None" << endl;
		} else {
			cout << "Prerequisites: ";
			for (const string& prereq : course->prerequisites) {
				cout << prereq << ", ";
			}
			cout << endl;
		}
	}
};

// ------------- QuickSort and BinarySearch ----------------
/*
* QuickSort is used to sort courseIDs vector from first pass.
* BinarySearch is used to check if a prerequisite exists in the courseIDs vector.
* 
* Together verification of prerequisites is done at O(n log n) for sorting and O(log n) for searching.
*/

/*
* QuickSort function to sort a vector of strings.
* @param arr: Vector of strings to be sorted
* @param low: Starting index of the vector
* @param high: Ending index of the vector
*/
void QuickSort(vector<string>& arr, int low, int high) {
	if (low >= high) return;

	string pivot = arr[high];
	int i = low - 1;

	for (int j = low; j < high; j++) {
		if (arr[j] < pivot) {
			i++;
			swap(arr[i], arr[j]);
		}
	}

	swap(arr[i + 1], arr[high]);
	int pivotIndex = i + 1;

	QuickSort(arr, low, pivotIndex - 1);
	QuickSort(arr, pivotIndex + 1, high);
}
/*
* BinarySearch function to check if a key exists in a sorted vector of strings.
* @param arr: Sorted vector of strings
* @param key: The string to search for
* 
* @return: true if key is found, false otherwise
*/
bool BinarySearch(const vector<string>& arr, const string& key) {
	int left = 0;
	int right = arr.size() - 1;

	while (left <= right) {
		int mid = left + (right - left) / 2;

		if (arr[mid] == key) return true;
		else if (arr[mid] < key) {
			left = mid + 1;
		}
		else {
			right = mid - 1;
		}
	}
	return false; // Key not found
}

// ------------- Global Functions ----------------
vector<Course> Courses;
BinarySearchTree courseTree;

// ------------- Load Courses from File ----------------
void LoadCourses(const string& fileName) {
	vector<string> courseIDs;

	// --------- First Pass: Read Courses and Store IDs ---------
	ifstream file(fileName);
	if (!file.is_open()) {
		cout << "Error: File not open" << endl;
		return;

	}

	string lineIn;
	while (getline(file, lineIn)) {
		stringstream ss(lineIn);
		string token;
		if (!getline(ss, token, ',')) continue; // Only keep Course ID
		courseIDs.push_back(token);
	}
	file.close();

	// Sort course IDs for quick binary search later
	QuickSort(courseIDs, 0, courseIDs.size() - 1);

	// --------- Second Pass: Parse and Store courses ---------
	file.open(fileName);
	if (!file.is_open()) {
		cout << "Error: File not open" << endl;
		return;
	}

	
	while (getline(file, lineIn)) {
		stringstream ss(lineIn);
		string token;
		vector<string> tokens; // To hold the split tokens

		// Split the line by commas
		while (getline(ss, token, ',')) {
			tokens.push_back(token);
		}

		// Ensure we have at least 2 tokens (ID and Name)
		if (tokens.size() < 2) {
			cout << "Error: Invalid Format" << endl;
			continue;
		}

		Course newCourse;
		newCourse.courseID = tokens[0];
		newCourse.courseName = tokens[1];

		// Check for prerequisites if they exist
		if (tokens.size() > 2) {
			for (size_t i = 2; i < tokens.size(); ++i) {
				string prereq = tokens[i];
				if (BinarySearch(courseIDs, prereq)) {
					newCourse.prerequisites.push_back(prereq);
				}
			}
		}

		// Add the course to the global vector and insert into the BST
		Courses.push_back(newCourse);
		courseTree.InsertCource(newCourse);

	}
	file.close();
}

// ------------- Main Function ----------------

// Displays main menu options
void MainMenu() {
	
	cout << "1. Load Data Structure.\n";
	cout << "2. Print Course List.\n";
	cout << "3. Print Course\n";
	cout << "9) Exit\n";
	cout << "What would you like to do? ";
}

string ToUpper(const string& str) {
	string result = str;
	transform(result.begin(), result.end(), result.begin(), ::toupper);
	return result;
}

int main() {
	int userInput;
	string courseToFind;

	//Set filepath
	string filePath = "ABCU_Advising_Program_Input.csv";
	
	do {
		MainMenu();
		cin >> userInput;

		if (cin.fail()) { //Handle invalid input
			cin.clear(); // Clear the fail state
			cin.ignore(numeric_limits<streamsize>::max(), '\n');
			cout << userInput << "is not a valid option." << endl;
			continue;
		}

		switch (userInput) {
			case 1:
				LoadCourses(filePath);
				cout << "Courses loaded successfully." << endl;
				break;

			case 2:
				cout << "Course List:\n";
				courseTree.PrintAllCourses();
				break;
			case 3:
				cout << "What course do you want to know about? ";
				cin >> courseToFind;
				courseToFind = ToUpper(courseToFind); // Handle user lowercase input
				courseTree.PrintCourseInfo(courseToFind);
				break;
			case 9:
				cout << "Thank you for using the course planner!" << endl;
				break;
			default:
				cout << userInput << " is not a valid option." << endl;
		}
	} while (userInput != 9);
	
	return 0;
}
