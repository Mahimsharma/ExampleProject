import React from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  ToastAndroid,
} from 'react-native';
import { check, PERMISSIONS, request } from 'react-native-permissions';
import { launchImageLibrary } from 'react-native-image-picker';
import ImagePicker from 'react-native-image-crop-picker';
import { Upload } from 'react-native-tus-client';

export default function App() {
  const showToastWithGravityAndOffset = () => {
    console.log('BUTTON PRESSED');
    RequestExternalStoragePermission();
  };

  const selectImageCallback = (response) => {
    console.log('SELECTED IMAGE', response);
    let file = response[0]?.path;
    file = file.replace('file://', '')
    console.log('SELECTED IMAGE', file);
    if (file) {
      const upload = new Upload(file, {
        endpoint: 'https://master.tus.io/files/', // use your tus server endpoint instead
        onError: (error) => console.log('error', error),
        onSuccess: () => {
          console.log('Upload completed. File url:', upload.url);
        },
        onProgress: (uploaded, total) =>
          console.log(`Progress: ${((uploaded / total) * 100) | 0}%`),
      });
      upload.start();
    }
  };

  const RequestExternalStoragePermission = () => {
    const mediaLibrary = 'storage';
    // check(PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE).then((response) => {
    //   console.log(response);
    // });
    request(PERMISSIONS.ANDROID.READ_EXTERNAL_STORAGE).then(
      (response) => {
        console.log(response);
        if (response === 'granted') {
          ImagePicker.openPicker({
            mediaType: 'any',
            multiple: true,
          }).then(selectImageCallback);
          // launchImageLibrary({ mediaType: 'mixed' }, selectImageCallback);
          ToastAndroid.showWithGravityAndOffset(
            'A wild toast appeared!',
            ToastAndroid.LONG,
            ToastAndroid.BOTTOM,
            25,
            50,
          );
        }
      },
    );
  };

  return (
    <View style={styles.container}>
      <View style={styles.uploadView}>
        <TouchableOpacity
          onPress={() => showToastWithGravityAndOffset()}
          style={{ backgroundColor: 'pink', flex: 1 }}
        >
          <Text style={styles.uploadButton}>Pick a photo</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  uploadView: {
    marginTop: 200,
    height: 50,
    width: 150,
    backgroundColor: 'blue',
  },
  uploadButton: {
    fontWeight: 'bold',
    fontSize: 20,
  },
  buttonContainer: {
    margin: 4,
    alignItems: 'center',
    backgroundColor: 'pink',
    justifyContent: 'center',
  },
});
