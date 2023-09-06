package fi.riista.common.domain.training.ui

object MockTrainingData {
    const val Trainings =
        """
        {
            "jhtTrainings": [
                {
                    "id": 123,
                    "trainingType": "LAHI",
                    "occupationType": "AMPUMAKOKEEN_VASTAANOTTAJA",
                    "date": "2021-10-21",
                    "location": "Kokkola"
                }
            ],
            "occupationTrainings": [
                {
                    "id": 333,
                    "trainingType": "SAHKOINEN",
                    "occupationType": "PETOYHDYSHENKILO",
                    "date": "2022-01-12"
                }
            ]
        }
        """

    const val TrainingsWithNoLocation =
        """
        {
            "jhtTrainings": [
                {
                    "id": 123,
                    "trainingType": "LAHI",
                    "occupationType": "AMPUMAKOKEEN_VASTAANOTTAJA",
                    "date": "2021-10-21",
                    "location": null
                }
            ],
            "occupationTrainings": []
        }
        """
}
