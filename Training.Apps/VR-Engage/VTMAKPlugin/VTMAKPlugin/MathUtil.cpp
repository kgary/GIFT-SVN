#include "Logger.h"
#include "MathUtil.h"

double reframe(double val, double srcMin, double srcMax, double dstMin, double dstMax) {
	LOG(DEBUG) << "reframe(" << val << ", " << srcMin << ", " << srcMax << ", " << dstMin << ", " << dstMax << ")" << std::endl;
	const auto srcDelta = srcMax - srcMin;
	const auto srcOffset = val - srcMin;
	const auto srcNormal = srcOffset / srcDelta;

	const auto dstDelta = dstMax - dstMin;
	const auto dstOffset = srcNormal * dstDelta;
	return dstOffset + dstMin;
}
