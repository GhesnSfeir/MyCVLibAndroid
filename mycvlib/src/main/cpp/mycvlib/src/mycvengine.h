//
// Created by Ghesn Sfeir on 01/07/2019.
//

#ifndef MYCVLIBANDROID_ENGINE_H
#define MYCVLIBANDROID_ENGINE_H

#include <string>

namespace mycvlib {
  class MyCVEngine {
    public:
      MyCVEngine() {}
      
      std::string getVersionString();
      
      int getAverageValue(int rows, int cols, void* dataAddress);
  };
}
#endif //MYCVLIBANDROID_ENGINE_H
