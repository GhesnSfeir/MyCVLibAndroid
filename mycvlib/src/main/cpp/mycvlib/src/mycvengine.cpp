//
// Created by Ghesn Sfeir on 01/07/2019.
//

#include "mycvengine.h"

#include <opencv2/opencv.hpp>

std::string mycvlib::MyCVEngine::getVersionString() {
  return cv::getVersionString();
}

int mycvlib::MyCVEngine::getAverageValue(int rows, int cols, void *dataAddress)
{
  cv::Mat src(rows, cols, CV_8UC1, dataAddress);
  cv::Mat dst;
  cv::resize(src, dst, cv::Size(480, 480));
  cv::Mat laplacian;
  for (int i=0; i < 10; i++)
    cv::Laplacian(src, laplacian, CV_16S, 1);
  cv::Scalar _mean = cv::mean(src);
  int mean = (int) _mean.val[0];
  return mean;
}
