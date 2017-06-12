# react-native-image-capinsets
adds support for a similar functionality as `<Image capInsets={...} />` to android.
behind the scenes it will generate a `NinePatchDrawable` and set as background for the android `ImageView`

## Installation

1. `npm i --save react-native-image-capinsets`
2. `react-native link react-native-image-capinsets`


## Examples

Android resource:
```javascript
import ImageCapInset from 'react-native-image-capinsets';

<ImageCapInset
	source={require('./bubble.png')}
	capInsets={{ top: 8, right: 8, bottom: 8, left: 8 }}
	/>
```

Local file:
```javascript
<ImageCapInset source={{uri: "/data/user/0/com.example/files/content/test.png"}}
			   resizeMode="stretch"
			   capInsets={{top: 8, right: 8, bottom: 8, left: 8}}
			   />
```

Remote file:
```javascript
<ImageCapInset source={{uri: "https://examplesite.com/test.png"}}
			   resizeMode="stretch"
			   capInsets={{top: 8, right: 8, bottom: 8, left: 8}}
			   />
```