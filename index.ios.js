import React, { Component } from 'react';
import {
  StyleSheet,
  ImageBackground,
  Image
} from 'react-native';

const styles = StyleSheet.create({

});

class ImageCapInset extends Component {
  render() {
    return (
      <ImageBackground
        {...this.props}
        resizeMode={Image.resizeMode.stretch}
      />
    );
  }
}

export default ImageCapInset;
