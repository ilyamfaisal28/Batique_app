# Batique
Batique is an android app to classify images of six Batik classes from Northern Coastal Region of Java Island and one class of images as negative. This project was built for my undergraduate thesis research.

In this research, the CNN method applied with the ResNet50V2, MobileNetV2, and VGG-19 architectures, comparing these three models and integrating them into an Android-based application for automating batik motif classification. 

## Dataset
Since it's hard to find image data for each batik class, so it really need some data augmentation to improve the models ability for this case.
Here is the dataset quantities before and after the application of data augmentation:<br>
<br>
<img width="360" alt="Table of Dataset" src="https://github.com/ilyamfaisal28/Batique_app/assets/89628535/2688bb7c-2ae7-4399-a522-9ac3c23e7a63">

These images, before augmentation process, were divided into training, validation, and testing sets with a ratio of 7:2:1, respectively. Subsequently, after applying the augmentation process, it resulted in a total of 2,589 training data, 740 validation data, and 371 testing data.

Here is the sample of the dataset:
|![Sample of Dataset](https://github.com/ilyamfaisal28/Batique_app/assets/89628535/19ebc4a6-84b8-485d-ad92-eb63284a73d2)|
|:--:| 
| *Sample of Dataset* |

## Model Comparison Result
|![TFLite Model Comparison](https://github.com/ilyamfaisal28/Batique_app/assets/89628535/892aa6b6-18bc-4fcc-b4f4-af50a627356d)|
|:--:| 
| *TFLite Model Comparison* |

## Batique Interface
|![Batique Interface](https://github.com/ilyamfaisal28/Batique_app/assets/89628535/beacc9d9-f544-496e-bc49-a71e7a538d01)|
|:--:| 
| *Batique Interface* |
