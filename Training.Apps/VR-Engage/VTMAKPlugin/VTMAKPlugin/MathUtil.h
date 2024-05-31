#pragma once

/// <summary>
/// Determines the proportional position of a value in a 'source' range within 
/// a 'destination' range.
/// </summary>
/// <param name='val'>The value whose corresponding position should be found.</param>
/// <param name='srcMin'>The minimum value of the 'source' range.</param>
/// <param name='srcMax'>The maximum value of the 'source' range.</param>
/// <param name='dstMin'>The minimum value of the 'destination' range.</param>
/// <param name='dstMax'>The maximum value of the 'destination' range.</param>
/// <returns>
/// The proportional position of <paramref name='val'/> within the destination range.
/// </returns>
/// <example>
/// <code>
/// int main() {
///		reframe(50, 0, 100, 0, 10); // Yields 5
/// }
/// </code>
/// </example>
double reframe(double val, double srcMin, double srcMax, double dstMin, double dstMax);
