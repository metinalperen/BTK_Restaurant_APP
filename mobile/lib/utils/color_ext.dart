import 'package:flutter/material.dart';

extension ColorExt on Color {
  /// Flutter 3.22+ için `withOpacity` muadili
  Color alpha(double a) => withValues(alpha: a);
}
