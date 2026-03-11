#pragma once

#include <memory>
#include <functional>
#include <string>

// Alias Ref to shared_ptr for now (easy to swap later)
template <typename T>
using Ref = std::shared_ptr<T>;

// Standard Kotlin-to-C++ type mappings
using Int = int32_t;
using Long = int64_t;
using Boolean = bool;
using Double = double;
using String = std::string;
using Unit = void;

// Helper for 'null' which maps to nullptr
#define null nullptr

// The base class for all Microkt classes (to support RC)
struct KtObject : public std::enable_shared_from_this<KtObject> {
    virtual ~KtObject() = default;
};

template <typename T>
struct  List {
    std::vector<T> _data;
    void add(T& data) {
        _data.push_back(data);
    }
};

template <typename T>
using MutableList = List<T>;
