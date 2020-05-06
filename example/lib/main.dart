import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:cameraimouplugin/cameraimouplugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _cameraBindResult = 'Unknown';
  String _cameraUnBindResult = 'Unknown';


  StreamSubscription _eventSubscription;

  initEvent() {
    _eventSubscription = _eventChannelFor()
        .receiveBroadcastStream()
        .listen(eventListener, onError: errorListener);
  }


  void eventListener(dynamic event){
    final Map<dynamic,dynamic> map = event;

    switch(map['event']){
      case 'token':
        setState(() {
          _platformVersion = map['value'];
        });
        break;
      case 'checkBindOrNot':
        setState(() {
          _cameraBindResult = map['value'];
        });

        if(_cameraBindResult == "设备绑定成功"){

        }

        break;
      case 'unBindDevice':
        setState(() {
          _cameraUnBindResult = map['value'];
        });

        if(_cameraUnBindResult == "解绑设备成功"){

        }
        break;
    }

  }

  void errorListener(Object obj){

  }

  EventChannel _eventChannelFor() {
    return EventChannel('cameraimouplugin_event');
  }


  @override
  void initState() {
    super.initState();

    initEvent();

    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await Cameraimouplugin.getAccessToken();
    } on PlatformException {
      platformVersion = 'sdk 初始化失败.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> bindCamera(String ssid,String ssidPwd,String deviceId) async {
    String bindResult;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      bindResult = await Cameraimouplugin.loginAndBind(ssid, ssidPwd, deviceId);
    } on PlatformException {
      bindResult = '摄像头绑定异常.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _cameraBindResult = bindResult;
    });
  }

  Future<void> unBindCamera(String deviceId) async {
    String unBindResult;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      unBindResult = await Cameraimouplugin.unBindDevice(deviceId);
    } on PlatformException {
      unBindResult = '解绑摄像头异常.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _cameraUnBindResult = unBindResult;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Center(
              child: Text('获取AccessToken: $_platformVersion\n'),
            ),

            FlatButton(
              onPressed: () => bindCamera("hgwl","hgwl1234567890","5E04159PAJE23AE"),
              child: Container(
                width: 200,
                height: 50,
                alignment: Alignment.center,
                decoration: BoxDecoration(color: Colors.pink),
                child: Text(
                  "绑定摄像头",
                  style: TextStyle(color: Colors.white, fontSize: 22),
                ),
              ),
            ),

            Text(
              '摄像头绑定结果：$_cameraBindResult',style: TextStyle(color: Colors.red, fontSize: 22),
            ),

            FlatButton(
              onPressed: () => unBindCamera("5E04159PAJE23AE"),
              child: Container(
                width: 200,
                height: 50,
                alignment: Alignment.center,
                decoration: BoxDecoration(color: Colors.pink),
                child: Text(
                  "解绑摄像头",
                  style: TextStyle(color: Colors.white, fontSize: 22),
                ),
              ),
            ),

            Text(
              '解绑摄像头结果：$_cameraUnBindResult',style: TextStyle(color: Colors.red, fontSize: 22),
            ),
          ],
        ),
      ),
    );
  }
}
