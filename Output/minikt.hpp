#pragma once

#include <memory>
#include <functional>
#include <string>

// Alias Ref to shared_ptr for now (easy to swap later)
template <typename T>
using Ref = std::shared_ptr<T>;

template <typename T>
using Func = std::function<T>;

// Standard Kotlin-to-C++ type mappings
using Int = int32_t;
using Long = int64_t;
using Boolean = bool;
using Double = double;
using String = std::string;
using Unit = void;

using Char = wchar_t;

struct File {
    String name;

    String readText();

    void writeText(const String & content);

    bool exists();
    bool isDirectory();
    bool isFile();
};


// Helper for 'null' which maps to nullptr
#define null nullptr

// The base class for all Microkt classes (to support RC)
struct KtObject : public std::enable_shared_from_this<KtObject> {
    virtual ~KtObject() = default;
};

using Any = std::shared_ptr<KtObject>;

template <typename T>
struct  List {
    std::vector<T> _data;
    void add(T& data) {
        _data.push_back(data);
    }
};

template <typename T>
using MutableList = List<T>;

template <typename T>
List<T> emptyList() {
    return {};
}
template <typename T>
MutableList<T> mutableListOf() {
    return {};
}

template <typename T>
using Array = List<T>;

struct  StringBuilder {
    String _text;
    StringBuilder& append(String text) {
        _text.append(text);
        return *this;
    }
};
