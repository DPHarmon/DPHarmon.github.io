#ifndef AVL_TREE_H
#define AVL_TREE_H

#include "Course.h"
#include <functional>
#include <string>

/*
 * AVLTree.h
 * ----------------------------------------------------------------------------
 *	Public interface for the self-balancing AVL tree keyed on
 *	Course::courseNumber. Guarantees O(log n) insert and find regardless of
 *	input order.
	Node type, height tracking,
 *	and rotation logic are encapsulated as private members.
 *
 * Date: 07/19/2026
 * Author: Dylan Harmon
 */
class AVLTree {
public:
	AVLTree();
	~AVLTree();

	// Disables the copy constructor
	AVLTree(const AVLTree&) = delete;
	AVLTree& operator=(const AVLTree&) = delete;

	// Inserts a course. Duplicate course numbers are ignored.
	void Insert(const Course& course);

	// Returns a pointer to the stored Course, or nullptr if
	// course not found.
	const Course* Find(const std::string& courseNumber) const;

	// In-order traversal - visits courses in alphanumeric order.
	// Uses a callback so the tree does not depend on iostream
	void ForEachInOrder(const std::function<void(const Course&)>& visit) const;

	bool Empty() const;

private:
	struct Node {
		Course course;
		Node* left;
		Node* right;
		int height;

		explicit Node(const Course& c)
			: course(c), left(nullptr), right(nullptr), height(1) { }
	};

	Node* root_;

	// Height / balancing helpers
	int Height(Node* node) const;
	int Balance(Node* node) const;
	void UpdateHeight(Node* node);

	// Rotations -- LL and RR are handled by a single rotation each
	// LR and RL are composed inside InsertNode
	Node* RotateRight(Node* y);
	Node* RotateLeft(Node* x);

	// Recursive helpers
	Node* InsertNode(Node* node, const Course& course);
	Node* FindNode(Node*, const std::string& courseNumber) const;
	void InOrder(Node* node, const std::function<void(const Course&)>& visit) const;
	void Destroy(Node* node);
};

#endif // !AVL_TREE_H

