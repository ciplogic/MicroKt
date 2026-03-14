#include "minikt.hpp"

#include <iostream>
#include <fstream>
#include <sstream>
#include <filesystem>

namespace fs = std::filesystem;


String File::readText() {
    std::ifstream ifs;
    ifs.open(this->name);

    std::stringstream buffer;
    buffer << ifs.rdbuf();
    std::string content = buffer.str();
    return content;
}

void File::writeText(const String &content) {
    std::ofstream file(name); // Opens for writing (truncates by default)

    if (file.is_open()) {
        file << content;
        file.close(); // Good practice, though RAII handles this at scope end
    }
}

bool File::exists() {
    return fs::exists(this->name);
}

bool File::isDirectory() {
    return fs::is_directory(this->name);
}

bool File::isFile() {
    return fs::is_regular_file(this->name);
}
