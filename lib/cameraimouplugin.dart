import 'dart:async';

import 'package:flutter/services.dart';

class Cameraimouplugin {
  static const MethodChannel _channel = const MethodChannel('cameraimouplugin');

  ///初始化获取AccessToken
  static Future<String> getAccessToken() async {
    final String version = await _channel.invokeMethod('init');
    return version;
  }

  ///绑定摄像头
  static Future<String> loginAndBind(String deviceId, String token) async {
    final String version = await _channel.invokeMethod(
        'bind_camera', <String, dynamic>{
      'deviceId': deviceId,
      'token': token
    });
    return version;
  }

  ///解绑摄像头
  static Future<String> unBindDevice(String deviceId, String token) async {
    final String version = await _channel.invokeMethod('un_bind_camera',
        <String, dynamic>{'deviceId': deviceId, 'token': token});
    return version;
  }
}
