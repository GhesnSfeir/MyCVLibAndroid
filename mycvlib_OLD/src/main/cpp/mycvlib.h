//
// Created by Ghesn Sfeir on 01/07/2019.
//

#ifndef MYCVLIBANDROID_MYCVLIB_H
#define MYCVLIBANDROID_MYCVLIB_H

namespace mycvlib {

class Engine {
  public:
    Engine(int mInt) : mInt(mInt) {}

    int getInt();
  private:
    int mInt;

}
}

#endif //MYCVLIBANDROID_MYCVLIB_H
